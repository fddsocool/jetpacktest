package com.frx.jetpro.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvishew.xlog.XLog;
import com.frx.jetpro.R;
import com.frx.jetpro.databinding.LayoutRefreshViewBinding;
import com.frx.libcommon.view.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbsListFragment<T, M extends AbsViewModel<T>> extends Fragment implements OnRefreshListener, OnLoadMoreListener {

    protected LayoutRefreshViewBinding mBinding;
    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;
    protected PagedListAdapter mAdapter;
    protected M mViewModel;
    private DividerItemDecoration mItemDecoration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mBinding = LayoutRefreshViewBinding.inflate(inflater, container, false);
        mBinding.getRoot().setFitsSystemWindows(true);

        mRecyclerView = mBinding.recyclerView;
        mRefreshLayout = mBinding.refreshLayout;
        mEmptyView = mBinding.emptyView;

        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);

        //默认给列表中的Item 一个 10dp的ItemDecoration
        mItemDecoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        mItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        mRecyclerView.addItemDecoration(mItemDecoration);

        afterCreateView();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //利用子类传递的泛型参数实例化出 AbsViewModel 对象。
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();

        for (Type argument : arguments) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                XLog.i(argument.getTypeName());
            } else {
                XLog.i(argument.toString());
            }
        }

        if (arguments.length > 1) {
            Type argument = arguments[1];
            Class modelClaz = ((Class) argument).asSubclass(AbsViewModel.class);
            mViewModel = (M) ViewModelProviders.of(this).get(modelClaz);

            //触发页面初始化数据加载的逻辑
            mViewModel.getPageData().observe(getViewLifecycleOwner(), new Observer<PagedList<T>>() {
                @Override
                public void onChanged(PagedList<T> pagedList) {
                    mAdapter.submitList(pagedList);
                }
            });

            //触发边界回调
            //监听分页时有无更多数据,以决定是否关闭上拉加载的动画
            mViewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean hasData) {
                    finishRefresh(hasData);
                }
            });
        }

        afterViewCreated();
    }

    protected abstract void afterCreateView();

    protected abstract void afterViewCreated();

    public abstract PagedListAdapter getAdapter();

    public void submitList(PagedList<T> result) {
        //只有当新数据集合大于0的时候，才调用adapter.submitList
        //否则可能会出现 页面----有数据----->被清空-----空布局
        if (result.size() > 0) {
            mAdapter.submitList(result);
        }
        finishRefresh(result.size() > 0);
    }

    public void finishRefresh(boolean hasData) {
        //获取当前数据集合
        PagedList<T> currentList = mAdapter.getCurrentList();
        //判断有无数据
        hasData = hasData || currentList != null && currentList.size() > 0;
        //获取刷新组件状态
        RefreshState state = mRefreshLayout.getState();
        if (state.isFooter && state.isOpening) {
            mRefreshLayout.finishLoadMore();
        } else if (state.isHeader && state.isOpening) {
            mRefreshLayout.finishRefresh();
        }

        if (hasData) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

}

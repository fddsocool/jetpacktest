package com.frx.jetpro.ui.detail;

import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frx.jetpro.R;
import com.frx.jetpro.databinding.LayoutFeedDetailBottomInateractionBinding;
import com.frx.jetpro.model.Comment;
import com.frx.jetpro.model.Feed;
import com.frx.libcommon.global.AppGlobals;
import com.frx.libcommon.utils.PixUtils;
import com.frx.libcommon.view.EmptyView;

public abstract class ViewHandler {

    protected FragmentActivity activity;

    //列表对象
    protected RecyclerView recyclerView;

    //底部互动区域对象
    protected LayoutFeedDetailBottomInateractionBinding feedDetailBottomInateractionBinding;

    protected Feed feed;

    protected FeedCommentAdapter listAdapter;

    private final FeedDetailViewModel feedDetailViewModel;

    private EmptyView mEmptyView;

    public ViewHandler(FragmentActivity activity) {
        this.activity = activity;
        feedDetailViewModel = new ViewModelProvider(this.activity,
                ViewModelProvider.AndroidViewModelFactory.getInstance(AppGlobals.getApplication()))
                .get(FeedDetailViewModel.class);
    }

    @CallSuper
    public void bindInitData(Feed feed) {
        feedDetailBottomInateractionBinding.setOwner(activity);
        this.feed = feed;
        //初始化列表
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(null);
        listAdapter = new FeedCommentAdapter(activity) {
            @Override
            public void onCurrentListChanged(@Nullable PagedList<Comment> previousList, @Nullable PagedList<Comment> currentList) {
                boolean empty = currentList == null || currentList.size() <= 0;
                handleEmpty(!empty);
            }
        };
        recyclerView.setAdapter(listAdapter);

        feedDetailViewModel.setItemId(this.feed.itemId);
        //注册数据源监和PageList的观察者
        feedDetailViewModel.getPageData().observe(activity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                listAdapter.submitList(comments);
                handleEmpty(comments.size() > 0);
            }
        });
    }

    public void handleEmpty(boolean hasData) {
        if (hasData) {
            if (mEmptyView != null) {
                listAdapter.removeHeaderView(mEmptyView);
            }
        } else {
            if (mEmptyView == null) {
                mEmptyView = new EmptyView(activity);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = PixUtils.dp2px(40);
                mEmptyView.setLayoutParams(layoutParams);
                mEmptyView.setTitle(activity.getString(R.string.feed_comment_empty));
            }
            listAdapter.addHeaderView(mEmptyView);
        }
    }
}

package com.frx.jetpro.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.frx.jetpro.model.Feed;
import com.frx.jetpro.ui.AbsListFragment;
import com.frx.jetpro.ui.MutableDataSource;
import com.frx.libnavannotation.FragmentDestination;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel, FeedAdapter.FeedViewHolder> {
    private static final String Tag = "HomeFragment";

    @Override
    protected void afterCreateView() {

    }

    @Override
    protected void afterViewCreated() {
        //1.触发数据更新
        //2.接受缓存数据
        mViewModel.getCacheLiveData().observe(this, new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                submitList(feeds);
            }
        });
    }

    @Override
    public PagedListAdapter<Feed, FeedAdapter.FeedViewHolder> getAdapter() {
        String feedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<Feed> currentList = mAdapter.getCurrentList();
        if (currentList == null && currentList.isEmpty()) {
            finishRefresh(false);
            return;
        }

        Feed feed = currentList.get(mAdapter.getItemCount() - 1);
        mViewModel.loadAfter(feed.id, new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull List<Feed> data) {
                if (data == null && data.isEmpty()) {
                    return;
                }

                MutableDataSource<Integer, Feed> dataSource = new MutableDataSource<>();
                dataSource.data.addAll(data);

                PagedList.Config config = mAdapter.getCurrentList().getConfig();
                PagedList<Feed> feedPagedList = dataSource.buildNewPagedList(config);
                submitList(feedPagedList);
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        // ViewModel的DataSource调用invalidate方法之后
        // Paging框架会重新创建一个DataSource重新调用它的loadInitial方法加载初始化数据
        mViewModel.getDataSource().invalidate();
    }

}
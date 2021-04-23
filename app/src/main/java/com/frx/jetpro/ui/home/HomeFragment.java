package com.frx.jetpro.ui.home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.frx.jetpro.exoplayer.PageListPlayDetector;
import com.frx.jetpro.exoplayer.PageListPlayManager;
import com.frx.jetpro.model.Feed;
import com.frx.jetpro.ui.AbsListFragment;
import com.frx.jetpro.ui.MutablePageKeyedDataSource;
import com.frx.libnavannotation.FragmentDestination;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel, FeedAdapter.FeedViewHolder> {
    private static final String Tag = "HomeFragment";

    private PageListPlayDetector pageListPlayDetector;
    private boolean shouldPause = true;

    private String feedType;

    public static HomeFragment newInstance(String feedType) {
        Bundle args = new Bundle();
        args.putString("feedType", feedType);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

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

        pageListPlayDetector = new PageListPlayDetector(this, mRecyclerView);

        getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_PAUSE) {
                    // 如果是跳转到详情页,咱们就不需要暂停视频播放了
                    // 如果是前后台切换或者去别的页面了都是需要暂停视频播放的
                    if (shouldPause) {
                        pageListPlayDetector.onPause();
                    }
                } else if (event == Lifecycle.Event.ON_RESUME) {
                    shouldPause = true;
                    // 由于沙发Tab的几个子页面复用了HomeFragment，我们需要判断下当前页面它是否有ParentFragment.
                    // 当且仅当它和它的ParentFragment均可见的时候，才能恢复视频播放
                    if (getParentFragment() != null) {
                        if (getParentFragment().isVisible() && isVisible()) {
                            pageListPlayDetector.onResume();
                        }
                    } else {
                        if (isVisible()) {
                            pageListPlayDetector.onResume();
                        }
                    }
                }
            }
        });
    }

    @Override
    public PagedListAdapter<Feed, FeedAdapter.FeedViewHolder> getAdapter() {
        feedType = getArguments() == null
                   ? "all"
                   : getArguments().getString("feedType");
        Toast.makeText(getContext(), "请求feedType：" + feedType, Toast.LENGTH_SHORT).show();
        return new FeedAdapter(getContext(), feedType) {
            @Override
            public void onViewAttachedToWindow(@NonNull FeedViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                if (holder.isVideoItem()) {
                    pageListPlayDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull FeedViewHolder holder) {
                pageListPlayDetector.removeTarget(holder.getListPlayerView());
            }

            /**
             * 在更新当前页面列表时调用
             */
            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                // 这个方法是在每调用adpater.submitList()时会触发
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                boolean isVideo = feed.itemType == Feed.TYPE_VIDEO;
                shouldPause = !isVideo;
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<Feed> currentList = mAdapter.getCurrentList();
        if (currentList == null && currentList.isEmpty()) {
            finishRefresh(false);
            return;
        }

        Feed feed = currentList.get(mAdapter.getItemCount() - 1);
        if (feed == null) {
            return;
        }

        mViewModel.loadAfter(feed.id, new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull List<Feed> data) {
                if (data.size() > 0) {
                    return;
                }

                if (mAdapter.getCurrentList() == null) {
                    return;
                }

                MutablePageKeyedDataSource<Integer, Feed> dataSource = new MutablePageKeyedDataSource<>();
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

    /**
     * 通过tab切换时会走onHiddenChanged而不是onPause方法，所以要在这里控制切换时视频的播放状态
     *
     * @param hidden boolean
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            pageListPlayDetector.onPause();
        } else {
            pageListPlayDetector.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //如果是前后台切换或者去别的页面了都是需要暂停视频播放的
        if (shouldPause) {
            pageListPlayDetector.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        shouldPause = true;
        //由于沙发Tab的几个子页面复用了HomeFragment
        //需要判断下当前页面它是否有ParentFragment
        if (getParentFragment() != null) {
            //getParentFragment() != null说明当前fragment被嵌套，需要双重判断
            //当且仅当它和它的ParentFragment均可见的时候，才能恢复视频播放
            if (getParentFragment().isVisible() && isVisible()) {
                pageListPlayDetector.onResume();
            }
        } else {
            //只需判断当前fragment可见的时候恢复视频播放
            if (isVisible()) {
                pageListPlayDetector.onResume();
            }
        }
    }

    @Override
    public void onDestroy() {
        PageListPlayManager.release(feedType);
        super.onDestroy();
    }
}
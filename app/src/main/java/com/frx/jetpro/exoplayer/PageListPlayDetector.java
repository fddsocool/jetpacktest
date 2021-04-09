package com.frx.jetpro.exoplayer;

import android.util.Pair;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表视频自动播放 检测逻辑
 */
public class PageListPlayDetector {

    private RecyclerView mRecyclerView;

    // 正在播放的目标
    private IPlayTarget playingTarget;

    // 播放视频的目标集合
    private List<IPlayTarget> targets = new ArrayList<>();

    // RecycleView的位置
    private Pair<Integer, Integer> rvLocation = null;

    public void addTarget(IPlayTarget target) {
        targets.add(target);
    }

    public void removeTarget(IPlayTarget target) {
        targets.remove(target);
    }

    private Runnable delayAutoPlay = new Runnable() {
        @Override
        public void run() {
            autoPlay();
        }
    };

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                autoPlay();
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dx == 0 && dy == 0) {
                //时序问题。当执行了AdapterDataObserver#onItemRangeInserted  可能还没有被布局到RecyclerView上。
                //所以此时 recyclerView.getChildCount()还是等于0的。
                //等childView 被布局到RecyclerView上之后，会执行onScrolled（）方法
                //并且此时 dx,dy都等于0
                postAutoPlay();
            } else {
                //如果有正在播放的,且滑动时被划出了屏幕则停止他
                if (playingTarget != null && playingTarget.isPlaying() && !isTargetInBounds(playingTarget)) {
                    playingTarget.inActive();
                }
            }
        }
    };

    public PageListPlayDetector(LifecycleOwner owner, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;

        // 监听宿主生命周期
        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    // 宿主销毁时
                    playingTarget = null;
                    targets.clear();
                    mRecyclerView.removeCallbacks(delayAutoPlay);
                    mRecyclerView.removeOnScrollListener(scrollListener);
                    owner.getLifecycle().removeObserver(this);
                }
            }
        });

        if (mRecyclerView == null || mRecyclerView.getAdapter() == null) {
            return;
        }

        // 监听宿主adapter的生命周期事件
        mRecyclerView.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                postAutoPlay();
            }
        });

        // 监听宿主RecyclerView的滑动事件
        mRecyclerView.addOnScrollListener(scrollListener);
    }

    private void postAutoPlay() {
        mRecyclerView.post(delayAutoPlay);
    }

    private void autoPlay() {
        if (targets.size() <= 0 || mRecyclerView.getChildCount() <= 0) {
            return;
        }
        if (playingTarget != null && playingTarget.isPlaying() && isTargetInBounds(playingTarget)) {
            return;
        }

        IPlayTarget activeTarget = null;
        // 遍历targets，获取屏幕中的第一个条目
        for (IPlayTarget target : targets) {
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }

        if (activeTarget != null) {
            if (playingTarget != null) {
                playingTarget.inActive();
            }
            playingTarget = activeTarget;
            activeTarget.onActive();
        }
    }

    /**
     * 检测 IPlayTarget 所在的 viewGroup 是否至少还有一半的大小在屏幕内
     *
     * @param target IPlayTarget
     * @return boolean
     */
    private boolean isTargetInBounds(IPlayTarget target) {
        ViewGroup viewGroup = target.getOwner();
        //获取RecycleView的位置
        ensureRecyclerViewLocation();

        if (!viewGroup.isShown() || !viewGroup.isAttachedToWindow()) {
            return false;
        }

        int[] location = new int[2];
        viewGroup.getLocationOnScreen(location);

        // 获取target的中心位置
        int center = location[1] + viewGroup.getHeight() / 2;
        //承载视频播放画面的ViewGroup它需要至少一半的大小 在RecyclerView上下范围内
        return center >= rvLocation.first && center <= rvLocation.second;
    }

    private Pair<Integer, Integer> ensureRecyclerViewLocation() {
        if (rvLocation == null) {
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);
            int top = location[1];
            int bottom = top + mRecyclerView.getHeight();
            rvLocation = new Pair(top, bottom);
        }
        return rvLocation;
    }

    public void onPause() {
        if (playingTarget != null) {
            playingTarget.inActive();
        }
    }

    public void onResume() {
        if (playingTarget != null) {
            playingTarget.onActive();
        }
    }
}

package com.frx.jetpro.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.frx.jetpro.R;
import com.frx.jetpro.databinding.ActivityFeedDetailTypeImageBinding;
import com.frx.jetpro.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.frx.jetpro.model.Feed;
import com.frx.jetpro.view.PPImageView;
import com.frx.libcommon.utils.PixUtils;

public class ImageViewHandler extends ViewHandler {

    protected final ActivityFeedDetailTypeImageBinding feedDetailTypeImageBinding;
    protected LayoutFeedDetailTypeImageHeaderBinding imageHeaderBinding;

    public ImageViewHandler(FragmentActivity activity) {
        super(activity);

        //初始化布局
        feedDetailTypeImageBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image);
        recyclerView = feedDetailTypeImageBinding.recyclerView;
        feedDetailBottomInateractionBinding = feedDetailTypeImageBinding.interactionLayout;
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        //添加文图详情页头部
        feedDetailTypeImageBinding.setFeed(this.feed);

        imageHeaderBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(LayoutInflater.from(activity), recyclerView, false);
        imageHeaderBinding.setFeed(this.feed);

        PPImageView headerImage = imageHeaderBinding.headerImage;
        headerImage.bindData(this.feed.width, this.feed.height,
                this.feed.width > this.feed.height ? 0 : 16, this.feed.cover);

        //给RecycleView添加header
        listAdapter.addHeaderView(imageHeaderBinding.getRoot());

        //添加滚动监听
        //列表滚动到一定区域，显示标题栏信息
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //判断当前header划出去的距离是否大于标题栏高度
                boolean visible = imageHeaderBinding.getRoot().getTop() <= -feedDetailTypeImageBinding.titleLayout.getMeasuredHeight();
                feedDetailTypeImageBinding.authorInfoLayout.getRoot().setVisibility(
                        visible ? View.VISIBLE : View.GONE);
                feedDetailTypeImageBinding.authorInfoLayout.actionClose.setVisibility(
                        visible ? View.VISIBLE : View.GONE);
                feedDetailTypeImageBinding.title.setVisibility(visible ? View.GONE : View.VISIBLE);

            }
        });

        feedDetailTypeImageBinding.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        feedDetailTypeImageBinding.authorInfoLayout.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }
}

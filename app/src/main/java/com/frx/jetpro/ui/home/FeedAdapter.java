package com.frx.jetpro.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.frx.jetpro.databinding.LayoutFeedTypeImageBinding;
import com.frx.jetpro.databinding.LayoutFeedTypeVideoBinding;
import com.frx.jetpro.model.Feed;
import com.frx.jetpro.ui.InteractionPresenter;
import com.frx.jetpro.ui.detail.FeedDetailActivity;
import com.frx.jetpro.view.ListPlayerView;
import com.frx.libcommon.extention.LiveDataBus;

public class FeedAdapter extends PagedListAdapter<Feed, FeedAdapter.FeedViewHolder> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final String mCategory;

    //事件总线观察者，实现事件刷新
    private FeedObserver feedObserver;

    protected FeedAdapter(Context context, String category) {

        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                //判断是否item相同
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                //判断内容是否相同
                return oldItem.equals(newItem);
            }
        });

        mInflater = LayoutInflater.from(context);
        mContext = context;
        mCategory = category;
    }

    @Override
    public int getItemViewType(int position) {
        Feed feed = getItem(position);
        //if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
        //    return R.layout.layout_feed_type_image;
        //} else if (feed.itemType == Feed.TYPE_VIDEO) {
        //    return R.layout.layout_feed_type_video;
        //} else {
        //    throw new IllegalArgumentException("无此类型布局：feed.itemType=>" + feed.itemType);
        //}

        return feed.itemType;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //ViewDataBinding binding = DataBindingUtil.inflate(mInflater, viewType, parent,
        //false);
        //return new ViewHolder(binding);
        ViewDataBinding binding;
        if (viewType == Feed.TYPE_IMAGE_TEXT) {
            binding = LayoutFeedTypeImageBinding.inflate(mInflater);
        } else if (viewType == Feed.TYPE_VIDEO) {
            binding = LayoutFeedTypeVideoBinding.inflate(mInflater);
        } else {
            throw new IllegalArgumentException("无此类型布局：feed.itemType=>" + viewType);
        }
        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Feed item = getItem(position);

        holder.bindData(item, mCategory);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedDetailActivity.startFeedDetailActivity(mContext, item, mCategory);
                onStartFeedDetailActivity(item);

                //绑定事件总线观察者
                if (feedObserver == null) {
                    feedObserver = new FeedObserver();
                    LiveDataBus.get().with(InteractionPresenter.DATA_FROM_INTERACTION)
                            .observe((LifecycleOwner) mContext, feedObserver);
                }
                feedObserver.setFeed(item);
            }
        });
    }

    public void onStartFeedDetailActivity(Feed feed) {

    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final ViewDataBinding mBinding;
        public ListPlayerView listPlayerView;

        public FeedViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bindData(Feed item, String category) {
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.width, item.height, 16, item.cover);
            } else if (mBinding instanceof LayoutFeedTypeVideoBinding) {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.setFeed(item);

                listPlayerView = videoBinding.listPlayerView;
                listPlayerView.bindData(category, item.width, item.height, item.cover,
                        item.url);
            }
        }

        public boolean isVideoItem() {
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView() {
            return listPlayerView;
        }
    }

    private static class FeedObserver implements Observer<Feed> {

        private Feed feed;

        public void setFeed(Feed feed) {
            this.feed = feed;
        }

        @Override
        public void onChanged(Feed feed) {
            if (this.feed.id != feed.id) {
                return;
            }
            this.feed.author = feed.author;
            this.feed.ugc = feed.ugc;
            this.feed.notifyChange();
        }
    }
}

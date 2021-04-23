package com.frx.jetpro.ui.detail;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.frx.jetpro.databinding.LayoutFeedCommentListItemBinding;
import com.frx.jetpro.model.Comment;
import com.frx.jetpro.ui.InteractionPresenter;
import com.frx.jetpro.ui.MutableItemKeyedDataSource;
import com.frx.jetpro.ui.login.UserManager;
import com.frx.libcommon.extention.AbsPagedListAdapter;
import com.frx.libcommon.utils.PixUtils;

public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.FeedCommentViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;

    protected FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {

            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected FeedCommentViewHolder onOriginalCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding feedCommentListItemBinding = LayoutFeedCommentListItemBinding.inflate(mInflater, parent, false);
        return new FeedCommentViewHolder(feedCommentListItemBinding.getRoot(), feedCommentListItemBinding);
    }

    @Override
    protected void onOriginalBindViewHolder(FeedCommentViewHolder holder, int position) {
        Comment item = getItem(position);
        //绑定数据
        holder.bindData(item);

        holder.feedCommentListItemBinding.commentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InteractionPresenter.deleteFeedComment(mContext, item.id, item.commentId)
                        .observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean success) {
                                if (success) {
                                    deleteAndRefreshList(item);
                                }
                            }
                        });
            }
        });
    }

    public void addAndRefreshList(Comment comment) {
        //获取原对象
        PagedList<Comment> currentList = getCurrentList();
        //创建一个MutableItemKeyedDataSource
        MutableItemKeyedDataSource<Integer, Comment> newDataSource =
                new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) currentList.getDataSource()) {
                    @NonNull
                    @Override
                    public Integer getKey(@NonNull Comment item) {
                        return item.id;
                    }
                };
        newDataSource.data.add(comment);
        newDataSource.data.addAll(currentList);
        PagedList<Comment> newList = newDataSource.buildNewPagedList(currentList.getConfig());
        //设置要显示的新列表
        submitList(newList);
    }

    public void deleteAndRefreshList(Comment comment) {
        //获取原对象
        PagedList<Comment> currentList = getCurrentList();
        //创建一个MutableItemKeyedDataSource
        MutableItemKeyedDataSource<Integer, Comment> newDataSource =
                new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) currentList.getDataSource()) {
                    @NonNull
                    @Override
                    public Integer getKey(@NonNull Comment item) {
                        return item.id;
                    }
                };
        for (Comment item : currentList) {
            if (comment != item) {
                newDataSource.data.add(item);
            }
        }
        PagedList<Comment> newList = newDataSource.buildNewPagedList(getCurrentList().getConfig());
        submitList(newList);
    }

    public static class FeedCommentViewHolder extends RecyclerView.ViewHolder {

        private final LayoutFeedCommentListItemBinding feedCommentListItemBinding;

        public FeedCommentViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            feedCommentListItemBinding = binding;
        }

        public void bindData(Comment item) {
            feedCommentListItemBinding.setComment(item);
            boolean self = item.author != null && TextUtils.equals(UserManager.get().getUserId(), item.author.userId);
            //作者图标显隐藏
            feedCommentListItemBinding.labelAuthor.setVisibility(self ? View.VISIBLE : View.GONE);
            //删除按钮显隐藏
            feedCommentListItemBinding.commentDelete.setVisibility(self ? View.VISIBLE : View.GONE);
            //设置评论附件
            if (!TextUtils.isEmpty(item.imageUrl)) {
                feedCommentListItemBinding.commentExt.setVisibility(View.VISIBLE);
                feedCommentListItemBinding.commentCover.setVisibility(View.VISIBLE);
                feedCommentListItemBinding.commentCover.bindData(item.width, item.height,
                        0, PixUtils.dp2px(200), PixUtils.dp2px(200), item.imageUrl);
                if (!TextUtils.isEmpty(item.videoUrl)) {
                    feedCommentListItemBinding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    feedCommentListItemBinding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                feedCommentListItemBinding.commentCover.setVisibility(View.GONE);
                feedCommentListItemBinding.videoIcon.setVisibility(View.GONE);
                feedCommentListItemBinding.commentExt.setVisibility(View.GONE);
            }
        }
    }
}

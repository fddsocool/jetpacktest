package com.frx.libcommon.extention;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 一个能够添加HeaderView,FooterView的PagedListAdapter。
 * 解决了添加HeaderView和FooterView时 RecyclerView定位不准确的问题
 */
public abstract class AbsPagedListAdapter<T, VH extends RecyclerView.ViewHolder> extends PagedListAdapter<T, VH> {

    private SparseArray<View> mHeaders = new SparseArray<>();
    private SparseArray<View> mFooters = new SparseArray<>();

    private int BASE_ITEM_TYPE_HEADER = 100000;
    private int BASE_ITEM_TYPE_FOOTER = 200000;

    protected AbsPagedListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public void addHeaderView(View view) {
        //判断View对象是否还没有处在mHeaders数组里面
        if (mHeaders.indexOfValue(view) < 0) {
            mHeaders.put(BASE_ITEM_TYPE_HEADER++, view);
            notifyDataSetChanged();
        }
    }

    public void addFooterView(View view) {
        //判断View对象是否还没有处在mFooters数组里面
        if (mFooters.indexOfValue(view) < 0) {
            mFooters.put(BASE_ITEM_TYPE_FOOTER++, view);
            notifyDataSetChanged();
        }
    }

    // 移除头部
    public void removeHeaderView(View view) {
        int index = mHeaders.indexOfValue(view);
        if (index > 0) {
            mHeaders.removeAt(index);
            notifyDataSetChanged();
        }
    }

    // 移除底部
    public void removeFooterView(View view) {
        int index = mFooters.indexOfValue(view);
        if (index > 0) {
            mFooters.removeAt(index);
            notifyDataSetChanged();
        }
    }

    public int getHeaderCount() {
        return mHeaders.size();
    }

    public int getFooterCount() {
        return mFooters.size();
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return itemCount + mHeaders.size() + mFooters.size();
    }

    /**
     * 获取具体item的个数
     *
     * @return int
     */
    public int getOriginalItemCount() {
        return getItemCount() - mHeaders.size() - mFooters.size();
    }

    @Override
    public int getItemViewType(int position) {
        //判断是否是header
        if (isHeaderPosition(position)) {
            return mHeaders.keyAt(position);
        }

        //判断是否是footer
        if (isFooterPosition(position)) {
            position = position - getOriginalItemCount() - mHeaders.size();
            return mFooters.keyAt(position);
        }

        //返回正常item
        position = position - mHeaders.size();
        return getOriginalItemViewType(position);
    }

    protected int getOriginalItemViewType(int position) {
        return 0;
    }

    private boolean isFooterPosition(int position) {
        return position >= getOriginalItemCount() + mHeaders.size();
    }

    private boolean isHeaderPosition(int position) {
        return position < mHeaders.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mHeaders.indexOfKey(viewType) >= 0) {
            View view = mHeaders.get(viewType);
            return (VH) new RecyclerView.ViewHolder(view) {
            };
        }

        if (mFooters.indexOfKey(viewType) >= 0) {
            View view = mFooters.get(viewType);
            return (VH) new RecyclerView.ViewHolder(view) {
            };
        }

        return onOriginalCreateViewHolder(parent, viewType);
    }

    protected abstract VH onOriginalCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (isHeaderPosition(position) || isFooterPosition(position)) {
            return;
        }
        //列表中正常类型的itemView的 position 咱们需要减去添加headerView的个数
        position = position - mHeaders.size();
        onOriginalBindViewHolder(holder, position);
    }

    protected abstract void onOriginalBindViewHolder(VH holder, int position);

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
        if (!isHeaderPosition(holder.getAdapterPosition()) && !isFooterPosition(holder.getAdapterPosition())) {
            this.onOriginalViewAttachedToWindow(holder);
        }
    }

    protected void onOriginalViewAttachedToWindow(@NonNull VH holder) {
    }

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(new AdapterDataObserverProxy(observer));
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (!isHeaderPosition(holder.getAdapterPosition()) && !isFooterPosition(holder.getAdapterPosition())) {
            this.onOriginalViewDetachedFromWindow((VH) holder);
        }
    }

    protected void onOriginalViewDetachedFromWindow(VH holder) {
    }

    //如果先添加了headerView，而后网络数据回来了再更新到列表上
    //由于Paging在计算列表上item的位置时，并不会顾及我们有没有添加headerView，就会出现列表定位的问题
    //实际上，RecyclerView#setAdapter方法，它会给Adapter注册了一个AdapterDataObserver
    //咱么可以代理registerAdapterDataObserver()传递进来的observer。在各个方法的实现中，把headerView的个数算上，再中转出去即可
    private class AdapterDataObserverProxy extends RecyclerView.AdapterDataObserver {
        private RecyclerView.AdapterDataObserver observer;

        public AdapterDataObserverProxy(RecyclerView.AdapterDataObserver observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged() {
            observer.onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            observer.onItemRangeChanged(positionStart + mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            observer.onItemRangeChanged(positionStart + mHeaders.size(),
                    itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            observer.onItemRangeInserted(positionStart + mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            observer.onItemRangeRemoved(positionStart + mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            observer.onItemRangeMoved(fromPosition + mHeaders.size(), toPosition + mHeaders.size(), itemCount);
        }
    }
}

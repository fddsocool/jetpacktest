package com.frx.jetpro.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public abstract class AbsViewModel<T> extends ViewModel {
    private DataSource<Integer, T> mDataSource;
    private final LiveData<PagedList<T>> mPageData;
    private final MutableLiveData<Boolean> mBoundaryPageData = new MutableLiveData<>();
    protected final PagedList.Config mConfig;

    public AbsViewModel() {
        mConfig = new PagedList.Config.Builder()
                //分页量
                .setPageSize(10)
                //初始加载量
                .setInitialLoadSizeHint(12).build();

        mPageData = new LivePagedListBuilder<>(mFactory, mConfig)
                .setInitialLoadKey(0)
                .setBoundaryCallback(boundaryCallback)
                .build();
    }

    DataSource.Factory<Integer, T> mFactory = new DataSource.Factory<Integer, T>() {
        @NonNull
        @Override
        public DataSource<Integer, T> create() {
            if (mDataSource == null || mDataSource.isInvalid()) {
                mDataSource = createDataSource();
            }
            return mDataSource;
        }
    };

    public abstract DataSource<Integer, T> createDataSource();

    PagedList.BoundaryCallback<T> boundaryCallback = new PagedList.BoundaryCallback<T>() {

        @Override
        public void onZeroItemsLoaded() {
            //新提交的PagedList中没有数据
            mBoundaryPageData.postValue(false);
        }

        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            //新提交的PagedList中第一条数据被加载到列表上
            mBoundaryPageData.postValue(true);
        }

        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            //新提交的PagedList中最后一条数据被加载到列表上
        }
    };

    public LiveData<PagedList<T>> getPageData() {
        return mPageData;
    }

    public LiveData<Boolean> getBoundaryPageData() {
        return mBoundaryPageData;
    }

    public DataSource<Integer, T> getDataSource() {
        return mDataSource;
    }

    //可以在这个方法里 做一些清理 的工作
    @Override
    protected void onCleared() {

    }
}

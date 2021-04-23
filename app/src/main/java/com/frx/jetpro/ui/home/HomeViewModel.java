package com.frx.jetpro.ui.home;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.frx.jetpro.model.Feed;
import com.frx.jetpro.ui.AbsViewModel;
import com.frx.jetpro.ui.MutablePageKeyedDataSource;
import com.frx.libnetwork.ApiResponse;
import com.frx.libnetwork.ApiService;
import com.frx.libnetwork.JsonCallback;
import com.frx.libnetwork.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {

    /**
     * 是否需要缓存
     */
    private volatile boolean witchCache = true;

    /**
     * 缓存数据
     */
    private MutableLiveData<PagedList<Feed>> mCacheLiveData = new MutableLiveData<>();

    /**
     * 这是分页加载标志位，防止多次加载
     */
    private AtomicBoolean mLoadAfter = new AtomicBoolean(false);

    @Override
    public DataSource<Integer, Feed> createDataSource() {
        return new FeedDataSource();
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return mCacheLiveData;
    }

    class FeedDataSource extends ItemKeyedDataSource<Integer, Feed> {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params,
                                @NonNull LoadInitialCallback<Feed> callback) {
            //加载初始化数据的
            loadData(0, params.requestedLoadSize, callback);
            witchCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //向后加载分页数据的
            loadData(params.key, params.requestedLoadSize, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //能够向前加载数据的
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            //返回最后一条item的信息
            return item.id;
        }
    }

    private void loadData(int key, int requestedLoadSize, ItemKeyedDataSource.LoadCallback<Feed> callback) {

        if (key > 0) {
            //如果key>0，表示此次加载是分页加载
            mLoadAfter.set(true);
        }

        Request request = ApiService.get("/feeds/queryHotFeedsList").addParam("feedType", null).addParam("userId", 0)
                                    .addParam("feedId", key).addParam("pageCount", requestedLoadSize).responseType(
                        new TypeReference<ArrayList<Feed>>() {}.getType());

        if (witchCache) {
            request.setCacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    Log.e("loadData", "onCacheSuccess");
                    //此处需要把List类型的对象转换为PagingList类型对象
                    List<Feed> body = response.body;
                    MutablePageKeyedDataSource<Integer, Feed> dataSource = new MutablePageKeyedDataSource<>();
                    dataSource.data.addAll(body == null
                                           ? Collections.emptyList()
                                           : body);
                    PagedList<Feed> pagedList = dataSource.buildNewPagedList(mConfig);
                    //在子线程中用postValue
                    mCacheLiveData.postValue(pagedList);
                }
            });
        }

        //如果获取过缓存则需要clone之前的request
        try {
            Request netRequest = witchCache
                                 ? request.clone()
                                 : request;
            netRequest.setCacheStrategy(key == 0
                                        ? Request.NET_CACHE
                                        : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();
            List<Feed> data = response.body == null
                              ? Collections.emptyList()
                              : response.body;

            callback.onResult(data);

            if (key > 0) {
                //通过BoundaryPageData发送数据 告诉UI层 是否应该主动关闭上拉加载分页的动画
                ((MutableLiveData) getBoundaryPageData()).postValue(data.size() > 0);
                mLoadAfter.set(false);
            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 实现分页加载数据
     *
     * @param itemId   最后一个数据的id
     * @param callback 回调
     */
    @SuppressLint("RestrictedApi")
    public void loadAfter(int itemId, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (mLoadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }

        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(itemId, mConfig.pageSize, callback);
            }
        });
    }
}
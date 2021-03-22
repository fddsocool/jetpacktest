package com.frx.jetpro.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.frx.jetpro.model.Feed;
import com.frx.jetpro.ui.AbsViewModel;
import com.frx.libnetwork.ApiResponse;
import com.frx.libnetwork.ApiService;
import com.frx.libnetwork.JsonCallback;
import com.frx.libnetwork.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean witchCache = true;

    @Override
    public DataSource<Integer, Feed> createDataSource() {
        return mDataSource;
    }

    ItemKeyedDataSource<Integer, Feed> mDataSource = new ItemKeyedDataSource<Integer, Feed>() {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            //加载初始化数据的
            //先加载网络再更新缓存
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
    };

    private void loadData(int key, int requestedLoadSize,
                          ItemKeyedDataSource.LoadCallback<Feed> callback) {
        Request request = ApiService
                .get("/feeds/queryHotFeedsList")
                .addParams("feedType", null)
                .addParams("userId", 0)
                .addParams("feedId", key)
                .addParams("pageCount", requestedLoadSize)
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());

        if (witchCache) {
            request.setCacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    Log.e("loadData", "onCacheSuccess: ");
                }
            });
        }

        //如果获取过缓存则需要clone之前的request
        try {
            witchCache = false;
            Request netRequest = witchCache ? request.clone() : request;
            netRequest.setCacheStrategy(key == 0 ? Request.NET_CACHE : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;

            callback.onResult(data);

            if (key > 0) {
                //通过BoundaryPageData发送数据 告诉UI层 是否应该主动关闭上拉加载分页的动画
                ((MutableLiveData) getBoundaryPageData()).postValue(data.size() > 0);
            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
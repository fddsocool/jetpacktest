package com.frx.jetpro.ui.detail;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.elvishew.xlog.XLog;
import com.frx.jetpro.model.Comment;
import com.frx.jetpro.ui.AbsViewModel;
import com.frx.jetpro.ui.login.UserManager;
import com.frx.libnetwork.ApiResponse;
import com.frx.libnetwork.ApiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedDetailViewModel extends AbsViewModel<Comment> {

    private long itemId;

    @Override
    public DataSource<Integer, Comment> createDataSource() {
        return new FeedDetailDataSource();
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    static class FeedDetailDataSource extends ItemKeyedDataSource<Integer, Comment> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Comment> callback) {
            loadData(params.requestedInitialKey, params.requestedLoadSize, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            if (params.key > 0) {
                loadData(params.key, params.requestedLoadSize, callback);
            }
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Comment item) {
            return item.id;
        }

        private void loadData(Integer key, int requestedLoadSize, LoadCallback<Comment> callback) {
            ApiResponse<List<Comment>> response = ApiService.get("/comment/queryFeedComments")
                    .addParam("id", key)
                    .addParam("itemId", "123456")
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("pageCount", requestedLoadSize)
                    .responseType(new TypeReference<ArrayList<Comment>>() {
                    }.getType())
                    .execute();
            List<Comment> list = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(list);
        }
    }
}

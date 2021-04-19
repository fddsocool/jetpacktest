package com.frx.jetpro.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.frx.jetpro.model.Comment;
import com.frx.jetpro.model.Feed;
import com.frx.jetpro.model.User;
import com.frx.jetpro.ui.login.UserManager;
import com.frx.jetpro.ui.widget.ShareDialog;
import com.frx.libcommon.global.AppGlobals;
import com.frx.libnetwork.ApiResponse;
import com.frx.libnetwork.ApiService;
import com.frx.libnetwork.JsonCallback;

public class InteractionPresenter {

    private static final String URL_TOGGLE_FEED_LIK = "/ugc/toggleFeedLike";

    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    /**
     * 赞
     *
     * @param owner LifecycleOwner
     * @param feed  Feed
     */
    public static void toggleFeedLike(LifecycleOwner owner, Feed feed) {
        //判断用户是否登录
        if (UserManager.get().isLogin()) {
            toggleFeedLikeInternal(feed);
        } else {
            loginApp(owner, user -> toggleFeedLikeInternal(feed));
        }
    }

    private static void toggleFeedLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIK).addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId).execute(new JsonCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                if (response.body != null) {
                    boolean hasLiked = response.body.getBoolean("hasLiked");
                    feed.getUgc().setHasLiked(hasLiked);
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> response) {
                showToast(response.message);
            }
        });
    }

    /**
     * 踩
     *
     * @param owner LifecycleOwner
     * @param feed  Feed
     */
    public static void toggleFeedDiss(LifecycleOwner owner, Feed feed) {
        if (UserManager.get().isLogin()) {
            toggleFeedDissInternal(feed);
        } else {
            loginApp(owner, user -> toggleFeedDissInternal(feed));
        }
    }

    private static void toggleFeedDissInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS).addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId).execute(new JsonCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                if (response.body != null) {
                    boolean hasLiked = response.body.getBoolean("hasLiked");
                    feed.getUgc().setHasdiss(hasLiked);
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> response) {
                showToast(response.message);
            }
        });
    }

    /**
     * 给一个帖子的评论点赞/取消点赞
     *
     * @param owner   LifecycleOwner
     * @param comment Comment
     */
    public static void toggleCommentLike(LifecycleOwner owner, Comment comment) {
        if (UserManager.get().isLogin()) {
            toggleCommentLikeInternal(comment);
        } else {
            loginApp(owner, user -> toggleCommentLikeInternal(comment));
        }
    }

    private static void toggleCommentLikeInternal(Comment comment) {
        ApiService.get(URL_TOGGLE_COMMENT_LIKE)
                .addParam("commentId", comment.commentId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            comment.getUgc().setHasLiked(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    /**
     * 收藏、取消收藏一个帖子
     *
     * @param owner LifecycleOwner
     * @param feed  Feed
     */
    public static void toggleFeedFavorite(LifecycleOwner owner, Feed feed) {
        if (UserManager.get().isLogin()) {
            toggleFeedFavorite(feed);
        } else {
            loginApp(owner, user -> toggleFeedFavorite(feed));
        }
    }

    private static void toggleFeedFavorite(Feed feed) {
        ApiService.get("/ugc/toggleFavorite")
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFavorite = response.body.getBooleanValue("hasFavorite");
                            feed.getUgc().setHasFavorite(hasFavorite);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    /**
     * 关注/取消关注一个用户
     *
     * @param owner LifecycleOwner
     * @param feed  Feed
     */
    public static void toggleFollowUser(LifecycleOwner owner, Feed feed) {
        if (UserManager.get().isLogin()) {
            toggleFollowUser(feed);
        } else {
            loginApp(owner, user -> toggleFollowUser(feed));
        }
    }

    private static void toggleFollowUser(Feed feed) {
        ApiService.get("/ugc/toggleUserFollow")
                .addParam("followUserId", UserManager.get().getUserId())
                .addParam("userId", feed.author.userId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFollow = response.body.getBooleanValue("hasLiked");
                            feed.getAuthor().setHasFollow(hasFollow);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    public static void openShare(Context context, Feed feed) {
        String shareContent = feed.feeds_text;
        if (!TextUtils.isEmpty(feed.url)) {
            shareContent = feed.url;
        } else if (!TextUtils.isEmpty(feed.cover)) {
            shareContent = feed.cover;
        }
        ShareDialog shareDialog = new ShareDialog(context);
        shareDialog.setShareContent(shareContent);
        shareDialog.setListener(v -> {
        });
        shareDialog.show();
    }

    @SuppressLint("RestrictedApi")
    private static void showToast(String message) {
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
            Toast.makeText(AppGlobals.getApplication(), message, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 判断用户是否登录
     *
     * @param owner    LifecycleOwner
     * @param observer Observer<User>
     */
    private static void loginApp(LifecycleOwner owner, Observer<User> observer) {
        LiveData<User> userLiveData = UserManager.get().login(AppGlobals.getApplication());
        if (owner == null) {
            userLiveData.observeForever(loginObserver(observer, userLiveData));
        } else {
            userLiveData.observe(owner, loginObserver(observer, userLiveData));
        }
    }

    /**
     * 重新实现Observer的onChanged方法，用于数据更新通知
     *
     * @param observer Observer<User>
     * @param liveData LiveData<User>
     * @return Observer<User>
     */
    private static Observer<User> loginObserver(Observer<User> observer, LiveData<User> liveData) {
        return new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null && observer != null) {
                    observer.onChanged(user);
                }
                liveData.removeObserver(this);
            }
        };
    }
}

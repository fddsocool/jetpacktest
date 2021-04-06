package com.frx.jetpro.ui.login;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.frx.jetpro.model.User;
import com.frx.libnetwork.cache.CacheManager;

public class UserManager {
    private static final String KEY_CACHE_USER = "cache_user";

    private static UserManager mUserManager = new UserManager();

    //用于登陆回调
    private MutableLiveData<User> mUserMutableLiveData = new MutableLiveData<>();
    private User mUser;

    public static UserManager get() {
        return mUserManager;
    }

    private UserManager() {
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache != null) {
            mUser = cache;
        }
    }

    /**
     * 持久化用户数据
     *
     * @param user User
     */
    public void save(User user) {
        CacheManager.saveCache(KEY_CACHE_USER, user);
        mUser = user;
        if (mUserMutableLiveData.hasObservers()) {
            mUserMutableLiveData.postValue(user);
        }
    }

    /**
     * 登陆界面入口
     *
     * @param context Context
     * @return LiveData<User>
     */
    public LiveData<User> login(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        //返回LiveData用于登陆回调
        return mUserMutableLiveData;
    }

    /**
     * 登出
     */
    public void logout() {
        CacheManager.delete(KEY_CACHE_USER, mUser);
        mUser = null;
    }

    public boolean isLogin() {
        return mUser != null;
    }

    public User getUser() {
        return isLogin() ? mUser : null;
    }

    public String getUserId() {
        return isLogin() ? mUser.userId : null;
    }
}

package com.frx.libnetwork;

import android.annotation.SuppressLint;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.frx.libnetwork.cache.CacheManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class Request<T, R extends Request> implements Cloneable {

    protected String mUrl;
    protected HashMap<String, String> mHeaders = new HashMap<>();
    protected HashMap<String, Object> mParams = new HashMap<>();
    private Type mType;
    private Class mClaz;

    private int mCacheStrategy = NET_ONLY;
    public static final int CACHE_ONLY = 1;
    public static final int CACHE_FIRST = 2;
    public static final int NET_ONLY = 3;
    public static final int NET_CACHE = 4;

    /**
     * 缓存策略
     * <code>CACHE_ONLY</code>      仅仅只访问本地缓存，即便本地缓存不存在，也不会发起网络请求
     * <code>CACHE_FIRST</code>     先访问缓存，同时发起网络的请求，成功后缓存到本地
     * <code>NET_ONLY</code>        仅仅只访问服务器，不存任何存储
     * <code>NET_CACHE</code>       先访问网络，成功后缓存到本地
     */
    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE})
    public @interface CacheStrategy {

    }

    public Request(String url) {
        mUrl = url;
    }

    public R addHeader(String key, String value) {
        mHeaders.put(key, value);
        return (R) this;
    }

    public R addParam(String key, Object value) {
        if (value == null) {
            return (R) this;
        }

        try {
            if (value.getClass() == String.class) {
                mParams.put(key, value);
            } else {
                Field type = value.getClass().getField("TYPE");
                Class claz = (Class) type.get(null);
                if (claz.isPrimitive()) {
                    mParams.put(key, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    public R responseType(Type type) {
        mType = type;
        return (R) this;
    }

    public R responseType(Class claz) {
        mClaz = claz;
        return (R) this;
    }

    public R setCacheStrategy(@CacheStrategy int cacheStrategy) {
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        //构造request body
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.sOkHttpClient.newCall(request);
        return call;
    }

    /**
     * 异步调用
     *
     * @param callback JsonCallback<T>
     */
    @SuppressLint("RestrictedApi")
    public void execute(final JsonCallback callback) {
        if (mCacheStrategy != NET_ONLY) {
            //获取缓存
            ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
                ApiResponse<T> response = readCache(generateCacheKey(mUrl, mParams));
                if (callback != null) {
                    callback.onCacheSuccess(response);
                }
            });
        }

        if (mCacheStrategy != CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> result = new ApiResponse<>();
                    result.status = -1;
                    result.message = e.getMessage();
                    callback.onError(result);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ApiResponse<T> result = parseResponse(response, callback);
                    if (result.success) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError(result);
                    }
                }
            });
        }
    }

    /**
     * 同步调用
     */
    @SuppressLint("RestrictedApi")
    public ApiResponse<T> execute() {
        if (mType == null) {
            throw new RuntimeException("同步调用时，response的返回值类型必须设置");
        }
        if (mCacheStrategy == NET_ONLY) {
            //获取缓存
            return readCache(generateCacheKey(mUrl, mParams));
        }

        if (mCacheStrategy != CACHE_ONLY) {
            ApiResponse<T> result;
            try {
                Response response = getCall().execute();
                result = parseResponse(response, null);
            } catch (Exception e) {
                result = new ApiResponse<>();
                result.message = e.getMessage();
                result.success = false;
                result.status = -1;
            }
            return result;
        }
        return null;
    }

    private ApiResponse<T> parseResponse(@NotNull Response response, JsonCallback<T> callback) {
        String msg = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiService.sConvert;

        try {
            String content = response.body().string();
            if (success) {
                if (callback != null) {
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) convert.convert(content, argument);
                } else if (mType != null) {
                    result.body = (T) convert.convert(content, mType);
                } else if (mClaz != null) {
                    result.body = (T) convert.convert(content, mClaz);
                } else {
                    throw new RuntimeException("无法解析Result Body");
                }
            } else {
                msg = content;
            }
        } catch (Exception e) {
            success = false;
            msg = e.getMessage();
            status = -1;
        }

        result.success = success;
        result.status = status;
        result.message = msg;

        if (mCacheStrategy != NET_ONLY && result.success && result.body instanceof Serializable) {
            saveCache(result.body);
        }

        return result;
    }

    private void saveCache(T body) {
        CacheManager.saveCache(generateCacheKey(mUrl, mParams), body);
    }

    private ApiResponse<T> readCache(String cacheKey) {
        Object cache = CacheManager.getCache(cacheKey);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "local cache";
        result.success = true;
        result.body = (T) cache;
        return result;
    }

    public String generateCacheKey(String url, Map<String, Object> params) {
        return UrlCreator.createUrlFromParams(url, params);
    }

    /**
     * 生成请求
     *
     * @param builder okhttp3.Request.Builder
     * @return okhttp3.Request
     */
    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request<T, R>) super.clone();
    }
}

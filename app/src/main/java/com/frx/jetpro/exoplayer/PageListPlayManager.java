package com.frx.jetpro.exoplayer;

import android.app.Application;

import com.elvishew.xlog.XLog;
import com.frx.libcommon.global.AppGlobals;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.util.HashMap;

/**
 * 能适应多个页面视频播放的 播放器管理者
 * 每个页面一个播放器
 * 方便管每个页面的暂停/恢复操作
 */
public class PageListPlayManager {
    //管理每个页面的状态
    private static HashMap<String, PageListPlay> sPageListPlayHashMap = new HashMap<>();

    //播放视频需要MediaSourceFactory
    private static final ProgressiveMediaSource.Factory mediaSourceFactory;

    static {
        Application application = AppGlobals.getApplication();
        //创建http视频资源如何加载的工厂对象
        DefaultHttpDataSourceFactory defaultHttpDataSourceFactory =
                new DefaultHttpDataSourceFactory(
                        Util.getUserAgent(application, application.getPackageName()));
        //创建缓存，指定缓存位置，和缓存策略，为最近最少使用原则，最大为200m
        Cache cache = new SimpleCache(application.getCacheDir(),
                new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200),
                new ExoDatabaseProvider(application));
        //把缓存对象cache和负责缓存数据读取、写入的工厂类CacheDataSinkFactory 相关联
        CacheDataSink.Factory cacheDataSink = new CacheDataSink.Factory().setCache(cache)
                .setFragmentSize(Long.MAX_VALUE);

        //创建能够 边播放边缓存的 本地资源加载和http网络数据写入的工厂类
        CacheDataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory().setCache(cache)
                .setUpstreamDataSourceFactory(defaultHttpDataSourceFactory)
                .setCacheReadDataSourceFactory(new FileDataSource.Factory())
                .setCacheWriteDataSinkFactory(cacheDataSink)
                .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
                .setEventListener(new CacheDataSource.EventListener() {
                    @Override
                    public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {
                        XLog.i("onCachedBytesRead: cacheSizeBytes=" + cacheSizeBytes + ",cachedBytesRead=" + cachedBytesRead);
                    }

                    @Override
                    public void onCacheIgnored(int reason) {
                        XLog.i("onCacheIgnored: reason=" + reason);
                    }
                });
        mediaSourceFactory = new ProgressiveMediaSource.Factory(cacheDataSourceFactory);
    }

    public static MediaSource createMediaSource(String url) {
        MediaItem mediaItem = MediaItem.fromUri(url);
        return mediaSourceFactory.createMediaSource(mediaItem);
    }

    /**
     * 获取对应pageName的PageListPlay
     *
     * @param pageName String
     * @return PageListPlay
     */
    public static PageListPlay get(String pageName) {
        PageListPlay pageListPlay = sPageListPlayHashMap.get(pageName);
        if (pageListPlay == null) {
            pageListPlay = new PageListPlay();
            sPageListPlayHashMap.put(pageName, pageListPlay);
        }
        return pageListPlay;
    }

    /**
     * 销毁对应pageName的PageListPlay
     *
     * @param pageName String
     */
    public static void release(String pageName) {
        PageListPlay pageListPlay = sPageListPlayHashMap.remove(pageName);
        if (pageListPlay != null) {
            pageListPlay.release();
        }
    }
}

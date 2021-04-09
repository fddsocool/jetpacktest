package com.frx.jetpro.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;

import com.frx.jetpro.R;
import com.frx.libcommon.global.AppGlobals;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class PageListPlay {

    public SimpleExoPlayer exoPlayer;
    public PlayerView playerView;
    public PlayerControlView controlView;
    public String playUrl;

    public PageListPlay() {
        Application application = AppGlobals.getApplication();
        //创建exoplayer播放器实例
        SimpleExoPlayer.Builder builder = new SimpleExoPlayer.Builder(application,
                new DefaultRenderersFactory(application), new DefaultExtractorsFactory());
        exoPlayer = builder.build();
        //加载布局层级优化之后的能够展示视频画面的View
        playerView = (PlayerView) LayoutInflater.from(application)
                .inflate(R.layout.layout_exo_player_view, null, false);
        //加载布局层级优化之后的视频播放控制器
        controlView = (PlayerControlView) LayoutInflater.from(application)
                .inflate(R.layout.layout_exo_player_contorller_view, null, false);
        //关联播放器
        playerView.setPlayer(exoPlayer);
        controlView.setPlayer(exoPlayer);
    }

    public void release() {

        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop(true);
            exoPlayer.release();
            exoPlayer = null;
        }

        if (playerView != null) {
            playerView.setPlayer(null);
            playerView = null;
        }

        if (controlView != null) {
            controlView.setPlayer(null);
            controlView.setVisibilityListener(null);
            controlView = null;
        }
    }

    /**
     * 切换与播放器exoplayer 绑定的exoplayerView。用于页面切换视频无缝续播的场景
     *
     * @param newPlayerView PlayerView
     * @param attach        boolean
     */
    public void switchPlayerView(PlayerView newPlayerView, boolean attach) {
        playerView.setPlayer(attach ? null : exoPlayer);
        newPlayerView.setPlayer(attach ? exoPlayer : null);
    }
}

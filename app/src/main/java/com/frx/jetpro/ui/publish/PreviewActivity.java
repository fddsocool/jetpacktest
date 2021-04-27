package com.frx.jetpro.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.frx.jetpro.R;
import com.frx.jetpro.databinding.ActivityLayoutPreviewBinding;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class PreviewActivity extends AppCompatActivity {
    public static final String KEY_PREVIEW_URL = "preview_url";
    public static final String KEY_PREVIEW_VIDEO = "preview_video";
    public static final String KEY_PREVIEW_BTN_TEXT = "preview_btn_text";
    public static final int REQ_PREVIEW = 1000;

    private ActivityLayoutPreviewBinding dataBinding;
    private SimpleExoPlayer player;

    public static void startActivityForResult(Activity activity, String previewUrl, boolean isVideo, String btnText) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(KEY_PREVIEW_URL, previewUrl);
        intent.putExtra(KEY_PREVIEW_VIDEO, isVideo);
        intent.putExtra(KEY_PREVIEW_BTN_TEXT, btnText);
        activity.startActivityForResult(intent, REQ_PREVIEW);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_layout_preview);
        String previewUrl = getIntent().getStringExtra(KEY_PREVIEW_URL);
        boolean isVideo = getIntent().getBooleanExtra(KEY_PREVIEW_VIDEO, false);
        String btnText = getIntent().getStringExtra(KEY_PREVIEW_BTN_TEXT);

        if (TextUtils.isEmpty(btnText)) {
            dataBinding.actionOk.setVisibility(View.GONE);
        } else {
            dataBinding.actionOk.setVisibility(View.VISIBLE);
            dataBinding.actionOk.setText(btnText);
            dataBinding.actionOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
            });
        }
        dataBinding.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (isVideo) {
            previewVideo(previewUrl);
        } else {
            previewImage(previewUrl);
        }
    }

    private void previewImage(String previewUrl) {
        dataBinding.photoView.setVisibility(View.VISIBLE);
        Glide.with(this).load(previewUrl).into(dataBinding.photoView);
    }

    private void previewVideo(String previewUrl) {
        dataBinding.playerView.setVisibility(View.VISIBLE);
        player = new SimpleExoPlayer.Builder(
                this,
                new DefaultRenderersFactory(this),
                new DefaultExtractorsFactory())
                .build();

        Uri uri = null;
        File file = new File(previewUrl);
        if (file.exists()) {
            DataSpec dataSpec = new DataSpec(Uri.fromFile(file));
            FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
                uri = fileDataSource.getUri();
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }
        } else {
            uri = Uri.parse(previewUrl);
        }

        ProgressiveMediaSource.Factory factory =
                new ProgressiveMediaSource.Factory(
                        new DefaultDataSourceFactory(
                                this,
                                Util.getUserAgent(this, getPackageName())
                        )
                );
        MediaItem mediaItem = MediaItem.fromUri(uri);
        ProgressiveMediaSource mediaSource = factory.createMediaSource(mediaItem);

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        dataBinding.playerView.setPlayer(player);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop(true);
            player.release();
        }
    }
}

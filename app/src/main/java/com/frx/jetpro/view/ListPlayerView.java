package com.frx.jetpro.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frx.jetpro.R;
import com.frx.libcommon.utils.PixUtils;

public class ListPlayerView extends FrameLayout {

    public View bufferView;
    public PPImageView cover;
    public PPImageView blur;
    protected ImageView playBtn;
    private String mCategory;
    private String mVideoUrl;

    public ListPlayerView(@NonNull Context context) {
        super(context);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        //缓冲转圈圈的view
        bufferView = findViewById(R.id.buffer_view);
        //封面view
        cover = findViewById(R.id.cover);
        //高斯模糊背景图,防止出现两边留嘿
        blur = findViewById(R.id.blur_background);
        //播放盒暂停的按钮
        playBtn = findViewById(R.id.play_btn);
    }

    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mVideoUrl = videoUrl;
        cover.setImageURI(cover, coverUrl, false);

        //高斯模糊 如果该视频的宽度小于高度，则高斯模糊背景图显示出来
        if (widthPx < heightPx) {
            blur.setBlurImageUrl(coverUrl, 10);
            blur.setVisibility(VISIBLE);
        } else {
            blur.setVisibility(INVISIBLE);
        }

        setSize(widthPx, heightPx);
    }

    private void setSize(int widthPx, int heightPx) {
        //最大宽高
        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = maxWidth;

        //计算后的宽高
        int layoutWidth = maxWidth;
        int layoutHeight = 0;

        //封面宽高
        int coverWidth;
        int coverHeight;

        if (widthPx >= heightPx) {
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) (heightPx / (widthPx * 1.0f / maxWidth));
        } else {
            coverWidth = (int) (widthPx / (heightPx * 1.0f / maxHeight));
            layoutHeight = coverHeight = maxHeight;
        }

        //播放容器
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = layoutWidth;
        params.height = layoutHeight;
        setLayoutParams(params);

        //高斯模糊背景图
        ViewGroup.LayoutParams blurParams = blur.getLayoutParams();
        blurParams.height = layoutHeight;
        blurParams.width = layoutWidth;
        blur.setLayoutParams(blurParams);

        //封面宽高
        FrameLayout.LayoutParams coverParams = (LayoutParams) cover.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeight;
        coverParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverParams);

        //播放按钮
        FrameLayout.LayoutParams playBtnParams = (LayoutParams) playBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playBtnParams);
    }
}

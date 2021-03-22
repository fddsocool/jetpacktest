package com.frx.jetpro.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.frx.libcommon.utils.PixUtils;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * 适配 data binding 的 ImageView
 */
public class PPImageView extends AppCompatImageView {

    public PPImageView(@NonNull Context context) {
        super(context);
    }

    public PPImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PPImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("CheckResult")
    @BindingAdapter(value = {"image_url", "isCircle"})
    public void setImageURI(PPImageView imageView, String imageUrl, boolean isCircle) {
        RequestBuilder<Drawable> builder = Glide.with(imageView).load(imageUrl);
        if (isCircle) {
            builder.transform(new CircleCrop());
        }

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();

        //防止图片尺寸过大
        if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
            builder.override(layoutParams.width, layoutParams.height);
        }

        builder.into(imageView);
    }

    public void bindData(int widthPx, int heightPx, int marginLeft, String imageUrl) {
        bindData(widthPx, heightPx, marginLeft, PixUtils.getScreenWidth(),
                PixUtils.getScreenHeight(), imageUrl);
    }

    public void bindData(int widthPx, int heightPx, int marginLeft, int maxWidth, int maxHeight,
                         String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            setVisibility(GONE);
            return;
        } else {
            setVisibility(VISIBLE);
        }

        if (widthPx <= 0 || heightPx <= 0) {
            Glide.with(this).load(imageUrl).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource,
                                            @Nullable Transition<? super Drawable> transition) {
                    int height = resource.getIntrinsicHeight();
                    int width = resource.getIntrinsicWidth();
                    setSize(width, height, marginLeft, maxWidth, maxHeight);
                    setImageDrawable(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
            return;
        }

        setSize(widthPx, heightPx, marginLeft, maxWidth, maxHeight);
        setImageURI(this, imageUrl, false);
    }

    private void setSize(int width, int height, int marginLeft, int maxWidth, int maxHeight) {
        int finalWidth;
        int finalHeight;
        if (width > height) {
            finalWidth = maxWidth;
            finalHeight = (int) (height / (width * 1.0f / finalWidth));
        } else {
            finalHeight = maxHeight;
            finalWidth = (int) (width / (height * 1.0f / finalHeight));
        }

        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = finalWidth;
        lp.height = finalHeight;

        if (lp instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) lp).leftMargin =
                    height > width ? PixUtils.dp2px(marginLeft) : 0;
        } else if (lp instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) lp).leftMargin =
                    height > width ? PixUtils.dp2px(marginLeft) : 0;
        }

        setLayoutParams(lp);
    }

    public void setBlurImageUrl(String coverUrl, int radius) {
        Glide.with(this).load(coverUrl).override(50)
                .transform(new BlurTransformation())
                .dontAnimate()
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }
}

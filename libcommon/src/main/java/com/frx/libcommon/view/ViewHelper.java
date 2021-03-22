package com.frx.libcommon.view;

import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.frx.libcommon.R;


public class ViewHelper {

    public static final int RADIUS_ALL = 0;
    public static final int RADIUS_LEFT = 1;
    public static final int RADIUS_TOP = 2;
    public static final int RADIUS_RIGHT = 3;
    public static final int RADIUS_BOTTOM = 4;

    public static void setViewOutLine(View view, AttributeSet attributes, int defStyleAttr, int defStyleRes) {
        TypedArray array = view.getContext().obtainStyledAttributes(attributes, R.styleable.viewOutLineStrategy, defStyleAttr, defStyleRes);
        int radius = array.getDimensionPixelSize(R.styleable.viewOutLineStrategy_clip_radius, 0);
        int hideSide = array.getInt(R.styleable.viewOutLineStrategy_clip_side, 0);
        array.recycle();
        setViewOutline(view, radius, hideSide);
    }

    public static void setViewOutline(View view, int radius, final int hideSide) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int w = view.getWidth();
                int h = view.getHeight();
                if (w == 0 || h == 0) {
                    return;
                }

                int left = 0;
                int top = 0;
                int right = w;
                int bottom = h;

                if (hideSide != RADIUS_ALL) {
                    if (hideSide == RADIUS_LEFT) {
                        right += radius;
                    } else if (hideSide == RADIUS_TOP) {
                        bottom += radius;
                    } else if (hideSide == RADIUS_RIGHT) {
                        left -= radius;
                    } else if (hideSide == RADIUS_BOTTOM) {
                        top -= radius;
                    }
                    outline.setRoundRect(left, top, right, bottom, radius);
                    return;
                }

                if (radius <= 0) {
                    outline.setRect(left, top, right, bottom);
                } else {
                    outline.setRoundRect(left, top, right, bottom, radius);
                }
            }
        });
        view.setClipToOutline(radius > 0);
        view.invalidate();
    }


}

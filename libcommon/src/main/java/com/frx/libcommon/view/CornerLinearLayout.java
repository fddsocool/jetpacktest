package com.frx.libcommon.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 圆角 LinearLayout
 */
public class CornerLinearLayout extends LinearLayout {
    public CornerLinearLayout(@NonNull Context context) {
        this(context, null);
    }

    public CornerLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornerLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CornerLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ViewHelper.setViewOutLine(this, attrs, defStyleAttr, defStyleRes);
    }

    public void setViewOutline(int radius, int radiusSide) {
        ViewHelper.setViewOutline(this, radius, radiusSide);
    }
}

package com.frx.libcommon.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StatusBar {
    /**
     * 6.0以上的沉浸式布局
     * 6.0以上才可以修改字体颜色
     *
     * @param activity Activity
     */
    public static void fitSystemBar(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        // 1.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN--能够使得我们的页面布局延伸到状态栏之下，但不会隐藏状态栏，
        // 也就相当于状态栏是遮盖在布局之上的，两者重叠
        // View.SYSTEM_UI_FLAG_FULLSCREEN -- 能够使得我们的页面布局延伸到状态栏，但是会隐藏状态栏。状态栏不显示，
        // View.SYSTEM_UI_FLAG_FULLSCREEN 作用等同于 WindowManager.LayoutParams.FLAG_FULLSCREEN
        // 2.View.SYSTEM_UI_FLAG_LAYOUT_STABLE 稳定的布局，兼顾虚拟导航键
        // 3.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 亮色的状态栏（白底黑字）
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        // 4.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS，允许对状态栏开启绘制
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // 5.指定状态栏颜色，默认是灰色
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 6.0及以上的状态栏色调
     *
     * @param activity
     * @param light    true:白底黑字,false:黑底白字
     */
    public static void lightStatusBar(Activity activity, boolean light) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int visibility = decorView.getSystemUiVisibility();
        if (light) {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(visibility);
    }
}

package com.frx.libcommon.global;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AppGlobals {

    private static Application sApplication;

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    public static Application getApplication() {
        if (sApplication == null) {
            //通过反射获取Application
            try {
                Method currentApplication = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication");
                sApplication = (Application) currentApplication.invoke(null, (Object[]) null);
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return sApplication;
    }

}

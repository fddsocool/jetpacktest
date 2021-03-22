package com.frx.jetpro;

import android.app.Application;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.frx.libnetwork.ApiService;

public class JokeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApiService.init("http://123.56.232.18:8080/serverdemo", null);

        LogConfiguration config = new LogConfiguration.Builder()
                .tag("fLogger")
                .enableThreadInfo()
                .enableStackTrace(1)
                .enableBorder()
                .logLevel(LogLevel.ALL)
                .build();
        XLog.init(config);
    }
}

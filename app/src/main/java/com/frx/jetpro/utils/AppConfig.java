package com.frx.jetpro.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.frx.jetpro.model.BottomBar;
import com.frx.jetpro.model.Destination;
import com.frx.jetpro.model.SofaTab;
import com.frx.libcommon.global.AppGlobals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class AppConfig {

    private static HashMap<String, Destination> sDestination;

    private static BottomBar sBottomBar;

    private static SofaTab sSofaTab;

    public static HashMap<String, Destination> getDestConfig() {
        if (sDestination == null) {
            String content = parseFile("destination.json");
            sDestination = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
            }.getType());
        }

        return sDestination;
    }

    public static SofaTab getSofaTabConfig() {
        if (sSofaTab == null) {
            String content = parseFile("sofa_tabs_config.json");
            sSofaTab = JSON.parseObject(content, SofaTab.class);
            Collections.sort(sSofaTab.tabs, new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs o1, SofaTab.Tabs o2) {
                    return o1.index - o2.index;
                }
            });
        }
        return sSofaTab;
    }

    private static String parseFile(String fileName) {
        AssetManager assets = AppGlobals.getApplication().getResources().getAssets();

        InputStream inputStream = null;
        BufferedReader inputBufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            inputStream = assets.open(fileName);
            inputBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = inputBufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputBufferedReader != null) {
                try {
                    inputBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return stringBuilder.toString();
    }

    public static BottomBar getBottomBar() {
        if (sBottomBar == null) {
            String content = parseFile("main_tabs_config.json");
            sBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return sBottomBar;
    }
}

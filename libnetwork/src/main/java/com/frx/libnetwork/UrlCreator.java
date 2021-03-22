package com.frx.libnetwork;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class UrlCreator {
    public static String createUrlFromParams(String url, Map<String, Object> params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url);

        if (url.indexOf("?") > 0 || url.indexOf("&") > 0) {
            stringBuilder.append("&");
        } else {
            stringBuilder.append("?");
        }

        for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
            try {
                String value = URLEncoder.encode(String.valueOf(paramEntry.getValue()), "UTF-8");
                stringBuilder.append(paramEntry.getKey()).append("=").append(value).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}

package com.frx.libnetwork;

public class GetRequest<T> extends Request<T, GetRequest> {

    public GetRequest(String url) {
        super(url);
    }

    @Override
    protected okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        //get 请求把参数拼接在 url后面
        return builder.get().url(UrlCreator.createUrlFromParams(mUrl, mParams)).build();
    }
}

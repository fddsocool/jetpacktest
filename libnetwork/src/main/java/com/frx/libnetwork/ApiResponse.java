package com.frx.libnetwork;

/**
 * 对网络请求返回体的包装
 */
public class ApiResponse<T> {

    public boolean success;
    public int status;
    public String message;
    public T body;

}

package com.incheck.api.utils;

public interface RestHttpClient {

    String get(String url);

    <T> T get(String url, Class<T> response);

    <T, R> T post(String url, R request, Class<T> response);

}

package com.github.mobile.android;

/**
 * Request future delivering a response
 *
 * @param <V>
 *            type of response data
 */
public interface RequestFuture<V> {

    /**
     * Callback that response for request was successfully obtained
     *
     * @param response
     */
    void success(V response);
}

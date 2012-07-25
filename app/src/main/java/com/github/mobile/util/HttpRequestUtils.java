/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.HOST_DEFAULT;

import com.github.kevinsawicki.http.HttpRequest;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Utilities for working with {@link HttpRequest} objects
 */
public class HttpRequestUtils {

    /**
     * Is the given request to a URL that can have github.com credentials
     * included with the request?
     *
     * @param request
     * @return true if secure, false otherwise
     */
    public static boolean isSecure(final HttpRequest request) {
        final HttpURLConnection connection = request.getConnection();
        return connection instanceof HttpsURLConnection
                && HOST_DEFAULT.equals(connection.getURL().getHost());
    }
}

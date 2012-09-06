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
package com.github.mobile.tests.util;

import android.test.AndroidTestCase;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.mobile.util.HttpRequestUtils;

/**
 * Unit tests of {@link HttpRequestUtils}
 */
public class HttpRequestUtilsTest extends AndroidTestCase {

    /**
     * Test secure requests
     */
    public void testSecureRequests() {
        HttpRequestUtils.isSecure(HttpRequest.get("https://github.com"));
        HttpRequestUtils.isSecure(HttpRequest
                .get("https://github.com/a/b/raw/1234"));
    }

    /**
     * Test insecure requests
     */
    public void testInsecureRequests() {
        HttpRequestUtils.isSecure(HttpRequest.get("http://github.com"));
        HttpRequestUtils.isSecure(HttpRequest.get("https://github.c0m"));
    }
}

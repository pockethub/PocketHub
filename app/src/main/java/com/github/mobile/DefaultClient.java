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
package com.github.mobile;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.FROYO;

import com.github.kevinsawicki.http.HttpRequest;

import java.net.HttpURLConnection;

import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * Default client used to communicate with GitHub API
 */
public class DefaultClient extends GitHubClient {

    private static final String USER_AGENT = "GitHubAndroid/1.0";

    static {
        // Disable http.keepAlive on Froyo and below
        if (SDK_INT <= FROYO)
            HttpRequest.keepAlive(false);
    }

    private final boolean useAcceptHeader;

    /**
     * Create client
     */
    public DefaultClient() {
        super();

        useAcceptHeader = true;
        setSerializeNulls(false);
        setUserAgent(USER_AGENT);
    }

    /**
     * Create client
     *
     * @param hostname
     * @param port
     * @param scheme
     */
    public DefaultClient(String hostname, int port, String scheme) {
        super(hostname, port, scheme);

        useAcceptHeader = false;
        setSerializeNulls(false);
        setUserAgent(USER_AGENT);
    }

    /**
     * Create client
     *
     * @param hostname
     */
    public DefaultClient(String hostname) {
        super(hostname);

        useAcceptHeader = false;
        setSerializeNulls(false);
        setUserAgent(USER_AGENT);
    }

    @Override
    protected HttpURLConnection configureRequest(HttpURLConnection request) {
        super.configureRequest(request);

        if (useAcceptHeader)
            request.setRequestProperty(HEADER_ACCEPT, "application/vnd.github.beta.full+json");

        return request;
    }
}

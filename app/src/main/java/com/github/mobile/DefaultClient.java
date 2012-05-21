/******************************************************************************
 *  Copyright (c) 2012 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *****************************************************************************/
package com.github.mobile;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.FROYO;
import android.util.Log;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * Default client used to communicate with GitHub API
 */
public class DefaultClient extends GitHubClient {

    private static final String TAG = "DefaultClient";

    private static final String USER_AGENT = "GitHubAndroid/1.0";

    private static final BigInteger SERIAL_NUMBER = new BigInteger("13785899061980321600472330812886105915");

    private static final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // Intentionally left blank
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            if (chain.length == 0)
                throw new SecurityException();

            X509Certificate cert = chain[chain.length - 1];
            if (cert == null)
                throw new SecurityException();

            if (!SERIAL_NUMBER.equals(cert.getSerialNumber()))
                throw new SecurityException();
        }
    } };

    private static final SSLSocketFactory SOCKET_FACTORY;

    static {
        SSLSocketFactory factory;
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAllCerts, new SecureRandom());
            factory = context.getSocketFactory();
        } catch (GeneralSecurityException e) {
            factory = null;
            Log.d(TAG, "Exception configuring certificate validation", e);
        }
        SOCKET_FACTORY = factory;
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

        if (SDK_INT <= FROYO && request instanceof HttpsURLConnection) {
            ((HttpsURLConnection) request).setSSLSocketFactory(SOCKET_FACTORY);
        }

        if (useAcceptHeader)
            request.setRequestProperty(HEADER_ACCEPT, "application/vnd.github.beta.full+json");

        return request;
    }
}

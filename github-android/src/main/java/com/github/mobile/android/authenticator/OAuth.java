package com.github.mobile.android.authenticator;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.io.Closeables.closeQuietly;

import com.google.common.io.CharStreams;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

public class OAuth {
    private final static String CLIENT_SECRET="943df1db0bfe39f1546d1e5861b3d3b7c99c9ab6";
    private final static String CLIENT_ID="ab8ebc8d13b54f42f1e6";
    public final static String ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    public final static String AUTH_URL = "https://github.com/login/oauth/authorize?client_id="+CLIENT_ID+"&scope=repo,gist";

    public static String accessTokenFor(String tempCode) {
        return parseAccessTokenFrom(responseFor(ACCESS_TOKEN_URL, accessTokenRequestDataFor(tempCode)));
    }

    public static String accessTokenRequestDataFor(String tempCode) {
        return "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&code=" + tempCode;
    }

    private static String parseAccessTokenFrom(String response) {
        return parseQueryString(response).get("access_token");
    }

    private static String responseFor(String url, String requestData) {
        InputStreamReader is = null;
        try {
            HttpsURLConnection c = httpPostConnectionFor(url);

            OutputStream os = c.getOutputStream();
            os.write(requestData.getBytes());
            os.flush();
            os.close();

            is = new InputStreamReader(c.getInputStream());
            return CharStreams.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(is);
        }
    }

    private static HttpsURLConnection httpPostConnectionFor(String url) throws IOException {
        HttpsURLConnection c = (HttpsURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setUseCaches(false);
        c.setDoInput(true);
        c.setDoOutput(true);
        return c;
    }

    private static Map<String, String> parseQueryString(String query) {
        Map<String, String> map = newHashMap();
        for (String p : query.split("&")) {
            String[] split = p.split("=");
            map.put(split[0], split[1]);
        }
        return map;
    }
}

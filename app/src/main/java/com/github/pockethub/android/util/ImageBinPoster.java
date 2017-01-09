package com.github.pockethub.android.util;

import android.content.Context;
import android.net.Uri;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.io.InputStream;

import okio.Okio;
import okio.Source;

public class ImageBinPoster {

    /**
     * Post the image to ImageBin
     *
     * @param context A context
     * @param uri The content URI
     * @param callback Request callback
     * @return If the file was successfully retrieved
     */
    public static boolean post(Context context, Uri uri, Callback callback) {
        byte[] bytes = null;

        try {
            InputStream stream = context.getContentResolver().openInputStream(uri);
            if (stream != null) {
                Source source = Okio.source(stream);
                bytes = Okio.buffer(source).readByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        post(bytes, callback);
        return true;
    }

    /**
     * Post the image to ImageBin
     *
     * @param bytes Bytes of the image to post
     * @param callback Request callback
     */
    public static void post(byte[] bytes, Callback callback) {
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", "test", RequestBody.create(MediaType.parse("image/*"), bytes))
                .build();

        Request request = new Request.Builder()
                .url("https://imagebin.ca/upload.php")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static String getUrl(String body) {
        String url = null;
        String[] pairs = body.split("\n");
        for (String string : pairs) {
            if (string.startsWith("url")) {
                int index = string.indexOf(":");
                url = string.substring(index + 1);
            }
        }
        return url;
    }
}

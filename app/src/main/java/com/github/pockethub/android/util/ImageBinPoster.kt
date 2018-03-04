package com.github.pockethub.android.util

import android.content.Context
import android.net.Uri
import okhttp3.*
import okio.Okio
import java.io.IOException

object ImageBinPoster {

    /**
     * Post the image to ImageBin
     *
     * @param context A context
     * @param uri The content URI
     * @param callback Request callback
     * @return If the file was successfully retrieved
     */
    @JvmStatic
    fun post(context: Context, uri: Uri, callback: Callback): Boolean {
        var bytes: ByteArray? = null

        try {
            val stream = context.contentResolver.openInputStream(uri)
            if (stream != null) {
                val source = Okio.source(stream)
                bytes = Okio.buffer(source).readByteArray()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        post(bytes, callback)
        return true
    }

    /**
     * Post the image to ImageBin
     *
     * @param bytes Bytes of the image to post
     * @param callback Request callback
     */
    @JvmStatic
    fun post(bytes: ByteArray?, callback: Callback) {
        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "test", RequestBody.create(MediaType.parse("image/*"), bytes!!))
                .build()

        val request = Request.Builder()
                .url("https://imagebin.ca/upload.php")
                .post(requestBody)
                .build()

        val client = OkHttpClient()
        val call = client.newCall(request)
        call.enqueue(callback)
    }

    @JvmStatic
    fun getUrl(body: String): String? {
        var url: String? = null
        val pairs = body.split("\n")
        for (string in pairs) {
            if (string.startsWith("url")) {
                val index = string.indexOf(":")
                url = string.substring(index + 1)
            }
        }
        return url
    }
}

package com.github.pockethub.android.util

import android.content.Context
import android.net.Uri
import io.reactivex.Single
import okhttp3.*
import okio.Okio
import java.io.IOException

object ImageBinPoster {

    private val client = OkHttpClient()

    /**
     * Post the image to ImageBin
     *
     * @param context A context
     * @param uri The content URI
     * @return Single containing the network Response
     */
    @JvmStatic
    fun post(context: Context, uri: Uri): Single<Response> {
        var bytes: ByteArray? = null

        try {
            val stream = context.contentResolver.openInputStream(uri)
            if (stream != null) {
                val source = Okio.source(stream)
                bytes = Okio.buffer(source).readByteArray()
            }
        } catch (e: IOException) {
            return Single.error(e)
        }

        return post(bytes)
    }

    /**
     * Post the image to ImageBin
     *
     * @param bytes Bytes of the image to post
     * @return Single containing the network Response
     */
    @JvmStatic
    fun post(bytes: ByteArray?): Single<Response> {
        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "test", RequestBody.create(MediaType.parse("image/*"), bytes!!))
                .build()

        val request = Request.Builder()
                .url("https://imagebin.ca/upload.php")
                .post(requestBody)
                .build()

        return Single.fromCallable { client.newCall(request).execute() }
    }

    @JvmStatic
    fun getUrl(body: String): String? {
        return body.split("\n").last { it.startsWith("url") }.substringAfter(':')
    }
}

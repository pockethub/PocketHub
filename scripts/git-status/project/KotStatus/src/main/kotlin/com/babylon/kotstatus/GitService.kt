package com.babylon.kotstatus

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface GitService {

    @Headers("User-Agent: kotStatus")
    @POST("repos/Babylonpartners/babylon-android/statuses/{commitId}")
    fun attachStatus(@Header("Authorization") authToken: String,
                     @Path("commitId") commitId: String,
                     @Body statusPost: StatusPost): Call<ResponseBody>
}

data class StatusPost(
        val context: String,
        val state: String,
        val description: String,
        val targetUrl: String)

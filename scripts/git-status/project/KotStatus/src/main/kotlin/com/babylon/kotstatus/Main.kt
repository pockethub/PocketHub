package com.babylon.kotstatus

import com.natpryce.konfig.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


fun main(args: Array<String>) {
    val contextArgKey = Key("context", stringType)
    val descriptionArgKey = Key("description", stringType)
    val (parsedConfig, parsedArgs) = parseArgs(args,
            CommandLineOption(contextArgKey, short = "c"),
            CommandLineOption(descriptionArgKey, short = "d"))

    val status = parsedArgs.first()
    val config = EnvironmentVariables() overriding parsedConfig


    val body = StatusPost(config[contextArgKey], status, config[descriptionArgKey], config[build.url])
    val call = gitService.invoke()
            .attachStatus("token ${config[jenkins.personal_access_token]}", config[git.commit], body)
    val response = call.execute()
    println(response.code())
}

val gitService = {
    val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY
    val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
            .build()

    val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    retrofit.create(GitService::class.java)
}

object git : PropertyGroup() {
    val commit by stringType
}

object build : PropertyGroup() {
    val url by stringType
}

object jenkins : PropertyGroup() {
    val personal_access_token by stringType
}
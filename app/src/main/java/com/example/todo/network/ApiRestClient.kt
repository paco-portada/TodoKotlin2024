package com.example.todo.network

import com.example.todo.Configuration
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient.Builder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiRestClient {
    private var API_SERVICE: ApiService? = null
    const val BASE_URL = Configuration.BASE_URL

    @get:Synchronized
    val instance: ApiService?
        get() {
            if (API_SERVICE == null) {
                val okHttpBuilder: Builder = Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)

                val gson = GsonBuilder()
                    .setDateFormat("dd-MM-yyyy")
                    .create()

                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpBuilder.build())
                    .build()

                API_SERVICE = retrofit.create(ApiService::class.java)
            }
            return API_SERVICE
        }
}
package com.example.todo.network

import com.example.todo.Configuration
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient.Builder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiTokenRestClient {
    private var API_SERVICE: ApiTokenService? = null
    const val BASE_URL = Configuration.BASE_URL

    @Synchronized
    fun getInstance(token: String?): ApiTokenService? {

        if (API_SERVICE == null) {
            val interceptor = Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            }

            val okHttpBuilder: Builder = Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(interceptor)

            val gson = GsonBuilder()
                .setDateFormat("dd-MM-yyyy")
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpBuilder.build())
                .build()

            API_SERVICE = retrofit.create(ApiTokenService::class.java)
        }
        return API_SERVICE
    }

    fun deleteInstance() {
        API_SERVICE = null
    }
}
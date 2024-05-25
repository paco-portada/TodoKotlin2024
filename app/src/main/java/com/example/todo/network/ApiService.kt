package com.example.todo.network

import com.example.todo.model.LoginResponse
import com.example.todo.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    //@POST("api/register")
    @FormUrlEncoded
    @POST("api/register")
    suspend fun register(
        @Field("name") name: String?,
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("confirm_password") confirmPassword: String?
    ): Response<RegisterResponse?>


    //@POST("api/login")
    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Response<LoginResponse?>

}
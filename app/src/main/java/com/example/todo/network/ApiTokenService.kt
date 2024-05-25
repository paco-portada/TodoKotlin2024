package com.example.todo.network

import com.example.todo.model.AddResponse
import com.example.todo.model.DeleteResponse
import com.example.todo.model.Email
import com.example.todo.model.EmailResponse
import com.example.todo.model.GetTasksResponse
import com.example.todo.model.LogoutResponse
import com.example.todo.model.Task
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiTokenService {
    @POST("api/logout")
    //@Header("Authorization") String token
    suspend fun logout(): Response<LogoutResponse?>

    // @get:GET("api/tasks")
    //@Header("Authorization") String token
    //val tasks: Response<GetTasksResponse?>

    @GET("api/tasks")
    //@Header("Authorization") String token
    suspend fun getTasks(): Response<GetTasksResponse?>

    @POST("api/tasks")
    //@Header("Authorization") String token,
    suspend fun createTask(
        @Body task: Task?
    ): Response<AddResponse?>

    @PUT("api/tasks/{id}")
    //@Header("Authorization") String token,
    suspend fun updateTask(
        @Body task: Task?,
        @Path("id") id: Long
    ): Response<AddResponse?>

    @DELETE("api/tasks/{id}")
    //@Header("Authorization") String token,
    suspend fun deleteTask(
        @Path("id") id: Long?
    ): Response<DeleteResponse?>

    @POST("api/email")
    suspend fun sendEmail(@Body email: Email?): Response<EmailResponse?>
}
package com.example.todo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GetTasksResponse {
    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("data")
    @Expose
    var data: ArrayList<Task>? = null

    @SerializedName("message")
    @Expose
    var message: String? = null
}
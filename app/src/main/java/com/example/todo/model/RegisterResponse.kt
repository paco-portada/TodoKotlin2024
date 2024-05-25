package com.example.todo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RegisterResponse {
    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("data")
    @Expose
    var data: RegisterData? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    // @SerializedName("info")
    // @Expose
    // var info: String? = null
}
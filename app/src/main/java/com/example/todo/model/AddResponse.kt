package com.example.todo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AddResponse {
    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("data")
    @Expose
    var data: AddData? = null

    @SerializedName("message")
    @Expose
    var message: String? = null
}
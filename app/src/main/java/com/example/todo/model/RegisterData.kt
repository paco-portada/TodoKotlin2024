package com.example.todo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RegisterData {
    @SerializedName("token")
    @Expose
    var token: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null
}
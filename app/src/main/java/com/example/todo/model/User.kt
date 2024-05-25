package com.example.todo.model

import com.google.gson.annotations.SerializedName

class User(
    @field:SerializedName("name") var name: String,
    @field:SerializedName("email") var email: String, @field:SerializedName("password") var password: String
)
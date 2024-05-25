package com.example.todo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

/**
 * Created by paco
 */
class Task : Serializable {
    var id: Long = 0

    var description: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    constructor(description: String?) : super() {
        this.description = description
    }

    constructor() {}

    override fun toString(): String {
        return description!!
    }
}
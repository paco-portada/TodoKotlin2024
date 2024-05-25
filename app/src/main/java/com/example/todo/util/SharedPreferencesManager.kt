package com.example.todo.util

import android.content.Context
import android.content.SharedPreferences
import com.example.todo.MainActivity

class SharedPreferencesManager(context: Context) {
    //public static final String APP = "MyApp";
    //public static final String EMAIL = "email";
    //public static final String PASSWORD = "password";
    //public static final String TOKEN = "token";
    var sp: SharedPreferences
    var spEditor: SharedPreferences.Editor

    init {
        sp = context.getSharedPreferences(MainActivity.APP, Context.MODE_PRIVATE)
        spEditor = sp.edit()
    }

    fun save(email: String?, password: String?) {
        spEditor.putString(MainActivity.EMAIL, email)
        spEditor.putString(MainActivity.PASSWORD, password)
        spEditor.apply()
    }

    fun save(email: String?, password: String?, token: String?) {
        spEditor.putString(MainActivity.EMAIL, email)
        spEditor.putString(MainActivity.PASSWORD, password)
        spEditor.putString(MainActivity.TOKEN, token)
        spEditor.apply()
    }

    fun saveEmail(key: String?, value: String?) {
        spEditor.putString(key, value)
        spEditor.apply()
    }

    val email: String?
        get() = sp.getString(MainActivity.EMAIL, "")

    fun savePassword(key: String?, value: String?) {
        spEditor.putString(key, value)
        spEditor.apply()
    }

    val password: String?
        get() = sp.getString(MainActivity.PASSWORD, "")

    fun saveToken(key: String?, value: String?) {
        spEditor.putString(key, value)
        spEditor.apply()
    }

    val token: String?
        get() = sp.getString(MainActivity.TOKEN, "")
}
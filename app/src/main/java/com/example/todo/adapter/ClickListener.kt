package com.example.todo.adapter

import android.view.View

/**
 * Created by paco on 6/02/18.
 */
interface ClickListener {
    fun onClick(view: View?, position: Int)
    fun onLongClick(view: View?, position: Int)
}
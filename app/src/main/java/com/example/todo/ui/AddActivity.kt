package com.example.todo.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todo.databinding.ActivityAddBinding
import com.example.todo.model.Task
import com.example.todo.network.ApiTokenRestClient
import com.example.todo.util.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var progressDialog: ProgressDialog
    lateinit var preferences: SharedPreferencesManager
    lateinit private var binding: ActivityAddBinding

    companion object {
        const val OK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml -> ActivityMainBinding
        binding = ActivityAddBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        binding!!.accept.setOnClickListener(this)
        binding!!.cancel.setOnClickListener(this)
        preferences = SharedPreferencesManager(this)
    }

    override fun onClick(v: View) {
        val description: String
        val task: Task
        if (v === binding!!.accept) {
            hideSoftKeyboard()
            description = binding!!.editText.text.toString()
            if (description.isEmpty())
                Toast.makeText(this,"Please, fill the description", Toast.LENGTH_SHORT).show()
            else {
                task = Task(description)
                connection(task)
            }
        } else if (v === binding!!.cancel) {
            finish()
        }
    }

    private fun connection(task: Task?) {
        showMessage(task!!.id.toString() + "")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setMessage("Connecting . . .")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()

        lifecycleScope.launch {
            try {
                val response = ApiTokenRestClient.getInstance(preferences!!.token)!!.createTask(task)

                launch(Dispatchers.Main) {
                    hideSoftKeyboard()
                    progressDialog!!.dismiss()
                    if (response.isSuccessful) {
                        val addResponse = response.body()
                        val i = Intent()
                        val bundle = Bundle()
                        bundle.putInt("id", addResponse!!.data!!.id!!)
                        bundle.putString("description", addResponse.data!!.description)
                        bundle.putString("createdAt", addResponse.data!!.createdAt)
                        i.putExtras(bundle)
                        setResult(OK, i)
                        finish()
                        showMessage("Task created ok")
                    } else {
                        var message = "Error creating the task"
                        if (!response.message()!!.isEmpty()) {
                            message += ": " + response.message()
                        }
                        showMessage(message)
                    }
                    }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    hideSoftKeyboard()
                    progressDialog!!.dismiss()
                    var message: String = "Failure in the communication\n"
                    if (e != null) {
                        Log.d("onFailure", e.message.toString())
                        message += e.message.toString()
                    }
                    showMessage(message)
                }
            }
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

}
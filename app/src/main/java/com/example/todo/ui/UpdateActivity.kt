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
import com.example.todo.databinding.ActivityUpdateBinding
import com.example.todo.model.Task
import com.example.todo.network.ApiTokenRestClient
import com.example.todo.util.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateActivity : AppCompatActivity(), View.OnClickListener {
    lateinit private var progressDialog: ProgressDialog
    lateinit var preferences: SharedPreferencesManager
    lateinit private var binding: ActivityUpdateBinding
    lateinit private var task: Task

    companion object {
        const val OK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml -> ActivityMainBinding
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)

        binding!!.accept.setOnClickListener(this)
        binding!!.cancel.setOnClickListener(this)
        preferences = SharedPreferencesManager(this)

        val i = intent
        task = i.getSerializableExtra("task") as Task
        binding!!.textViewId.text = task!!.id.toString()
        binding!!.editText.setText(task!!.description.toString())
    }

    override fun onClick(v: View) {
        val description: String
        hideSoftKeyboard()
        if (v === binding!!.accept) {
            description = binding!!.editText.text.toString()
            if (description.isEmpty())
                Toast.makeText(this,"Please, fill the description", Toast.LENGTH_SHORT).show()
            else {
                task!!.description = description
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
                val response = ApiTokenRestClient.getInstance(preferences!!.token)!!.updateTask(task, task.id!!)

                launch(Dispatchers.Main) {
                    hideSoftKeyboard()
                    progressDialog!!.dismiss()
                    if (response.isSuccessful) {
                        val updateResponse = response.body()
                        if (updateResponse!!.success!!) {
                            val i = Intent()
                            val bundle = Bundle()
                            bundle.putInt("id", updateResponse.data!!.id!!)
                            bundle.putString("description", updateResponse.data!!.description)
                            bundle.putString("createdAt", updateResponse.data!!.createdAt)
                            i.putExtras(bundle)
                            setResult(OK, i)
                            finish()
                            showMessage("Task updated ok: " + updateResponse.data!!.description)
                        } else {
                            var message = "Error updating the task"
                            if (response.message()!!.isNotEmpty()) {
                                message += ": " + response.message()
                            }
                            showMessage(message)
                        }
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
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}
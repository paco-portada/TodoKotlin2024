package com.example.todo.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todo.databinding.ActivityEmailBinding
import com.example.todo.model.Email
import com.example.todo.network.ApiTokenRestClient
import com.example.todo.util.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmailActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEmailBinding
    lateinit var preferences: SharedPreferencesManager
    private lateinit var progressDialog: ProgressDialog

    companion object {
        const val OK = 1
        const val EMAIL = "paco.portada@protonmail.com"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml -> ActivityMainBinding
        binding = ActivityEmailBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)

        binding!!.send.setOnClickListener(this)
        binding!!.cancel.setOnClickListener(this)
        preferences = SharedPreferencesManager(this)
    }

    override fun onClick(v: View) {
        hideSoftKeyboard()
        if (v === binding!!.send) {
            val from = binding!!.email.text.toString()
            val subject = binding!!.subject.text.toString()
            val message = binding!!.message.text.toString()
            if (from.isEmpty() || subject.isEmpty() || message.isEmpty()) {
                showMessage("Please, fill email, subject and message")
            } else {
                val email = Email(EMAIL, from, subject, message)
                connection(email)
            }
        } else if (v === binding!!.cancel) {
            finish()
        }
    }

    private fun connection(e: Email) {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setMessage("Connecting . . .")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()!!

        lifecycleScope.launch {
            try {
                val response = ApiTokenRestClient.getInstance(preferences!!.token)!!.sendEmail(e)

                launch(Dispatchers.Main) {
                    hideSoftKeyboard()
                    progressDialog!!.dismiss()
                    if (response.isSuccessful) {
                        val emailResponse = response.body()
                        if (emailResponse!!.success!!) {
                            //Intent i = new Intent();
                            //setResult(OK, i);
                            showMessage("Email sent ok: " + emailResponse.message)
                            finish()
                        }
                    } else {
                        var message = "Email not sent"
                        if (!response!!.message()!!.isEmpty()) {
                            message += ": " + response.message()
                        }
                        showMessage(message)
                        showMessage(message.toString())
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

    private fun showMessage(s: String?) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

}
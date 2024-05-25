package com.example.todo.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todo.MainActivity
import com.example.todo.network.ApiRestClient
import com.example.todo.util.SharedPreferencesManager
import com.example.todo.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private var binding: ActivityRegisterBinding? = null
    var preferences: SharedPreferencesManager? = null
    private var progressDialog: ProgressDialog? = null

    companion object {
        private const val TAG = "RegisterActivity"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml -> ActivityMainBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        binding!!.register.setOnClickListener(this)
        binding!!.login.setOnClickListener(this)
        preferences = SharedPreferencesManager(this)
        binding!!.email.setText(preferences!!.email)
        binding!!.password.setText(preferences!!.password)
    }

    override fun onClick(v: View) {
        if (v === binding!!.register) {
            val name = binding!!.name.text.toString()
            val email = binding!!.email.text.toString()
            val password = binding!!.password.text.toString()
            val confirmPassword = binding!!.confirmPassword.text.toString()
            Log.d(TAG, "Registro")
            if (validate(name, email, password, confirmPassword) == false) {
                showMessage("Register failed")
                binding!!.register.isEnabled = true
            } else {
                sendToServer(name, email, password, confirmPassword)
            }
        }
        if (v === binding!!.login) {
            // Finish the registration and return to the Login activity
            val intent = Intent(applicationContext, MainActivity::class.java)
            // Cancelar el registro
            setResult(RESULT_CANCELED, null)
            startActivity(intent)
            finish()
        }
    }

    fun validate(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var valid = true

        if (name.isEmpty() || name.length < 3) {
            binding!!.name.error = "at least 3 characters"
            requestFocus(binding!!.name)
            valid = false
        } else {
            binding!!.name.error = null
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding!!.email.error = "enter a valid email address"
            requestFocus(binding!!.email)
            valid = false
        } else {
            binding!!.email.error = null
        }
        if (password.isEmpty() || password.length < 4 || password.length > 10) {
            binding!!.password.error = "between 4 and 10 alphanumeric characters"
            requestFocus(binding!!.password)
            valid = false
        } else {
            binding!!.password.error = null
        }
        if (confirmPassword != password) {
            binding!!.confirmPassword.error = "Password Do not match"
            requestFocus(binding!!.confirmPassword)
            valid = false
        } else {
            binding!!.confirmPassword.error = null
        }
        return valid
    }

    private fun sendToServer(
        name: String,
        email: String,
        password: String,
        confirmPassord: String
    ) {
        progressDialog = ProgressDialog(this)
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setMessage("Creating Account ...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        binding!!.register.isEnabled = false


        lifecycleScope.launch {
            try {
                val response = ApiRestClient.instance!!.register(name, email, password, confirmPassord)

                launch(Dispatchers.Main) {
                    hideSoftKeyboard()
                    progressDialog!!.dismiss()
                    binding!!.register.isEnabled = true
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        if (registerResponse!!.success!!) {
                            //Log.d("onResponse", "" + registerResponse.getData().getToken());
                            //enviar al Login para entrar después de validar el email
                            binding!!.register.isEnabled = true
                            val resultIntent = Intent()
                            resultIntent.putExtra("email", email)
                            resultIntent.putExtra("password", password)
                            //guardar el token en shared preferences
                            resultIntent.putExtra("token", registerResponse.data!!.token)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            showMessage("Error in registration\n" + registerResponse.message)
                        }
                    } else {
                            val message = StringBuilder()
                            message.append("Failure in registration: ")
                            if (response.body() != null)
                                message.append("\n" + response.body()!!.toString());
                            if (response.errorBody() != null)
                                try {
                                    message.append("\n" + response.errorBody()!!.toString())
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            showMessage(message.toString())
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    hideSoftKeyboard()
                    progressDialog!!.dismiss()
                    binding!!.login.isEnabled = true
                    binding!!.register.isEnabled = true
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
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

}
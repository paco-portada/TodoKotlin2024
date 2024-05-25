package com.example.todo

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
import com.example.todo.network.ApiRestClient
import com.example.todo.ui.PanelActivity
import com.example.todo.ui.RegisterActivity
import com.example.todo.util.SharedPreferencesManager
import com.example.todo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var preferences: SharedPreferencesManager
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val APP = "ToDo App"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val TOKEN = "token"
        private const val TAG = "LoginActivity"
        private const val REQUEST_REGISTER = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml -> ActivityMainBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        preferences = SharedPreferencesManager(this)
        binding!!.email.setText(preferences!!.email)
        binding!!.password.setText(preferences!!.password)
        binding!!.login.setOnClickListener(this)
        binding!!.register.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        hideSoftKeyboard()
        if (v === binding!!.login) {
            if (validate() == false) {
                showMessage("Error al validar los datos")
            } else {
                loginByServer()
            }
        } else if (v === binding!!.register) {
            // Start the Register activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivityForResult(intent, REQUEST_REGISTER)
            //finish();
            //overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }

    private fun loginByServer() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setMessage("Login ...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        binding!!.login.isEnabled = false

        val email = binding!!.email.text.toString()
        val password = binding!!.password.text.toString()

        lifecycleScope.launch {
            try {
                val response = ApiRestClient.instance!!.login(email, password)

                launch(Dispatchers.Main) {
                    progressDialog!!.dismiss()
                    hideSoftKeyboard()
                    binding!!.login.isEnabled = true
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse!!.success!!) {
                            Log.d("onResponse", "" + response.body().toString())
                            //showMessage(response.body().getToken());
                            //guardar token en shared preferences
                            preferences.save(
                                binding.email.text.toString(),
                                binding.password.text.toString(),
                                loginResponse!!.data!!.token
                            )
                            startActivity(Intent(applicationContext, PanelActivity::class.java))
                            finish()
                        } else {
                            showMessage("Error in login: Email/Password incorrectos")
                            showMessage(loginResponse!!.message)
                            Log.d("Login error", loginResponse!!.message.toString())
                        }
                    } else {
                        val message = StringBuilder()
                        message.append("Failure in login: ")
                        if (response.body() != null) {
                            val loginResponse = response.body()
                            message.append("\n" + loginResponse!!.message);
                        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_REGISTER) {
            if (resultCode == RESULT_OK) {
                // TODO: Implement successful signup logic here
                // Por defecto se hace login automáticamente después del registro
                // Habría que validar el email antes de realizar login

                //Guardar token y lanzar Panel
                preferences!!.save(
                    data!!.extras!!.getString("email"),
                    data.extras!!.getString("password"),
                    data.extras!!.getString("token")
                )
                startActivity(Intent(this, PanelActivity::class.java))
                finish()
                //binding.inputEmail.setText(data.getExtras().getString("email"));
                //binding.inputPassword.setText(data.getExtras().getString("password"));
            } else if (requestCode == RESULT_CANCELED) {
                //no hacer nada, volver al login
                showMessage("Registro cancelado")
            }
        }
    }

    fun validate(): Boolean {
        var valid = true
        val email = binding!!.email.text.toString()
        val password = binding!!.password.text.toString()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding!!.email.error = "Enter a valid email address"
            requestFocus(binding!!.email)
            valid = false
        } else {
            binding!!.email.error = null
        }
        if (password.isEmpty()) {
            binding!!.password.error = "Password is empty"
            requestFocus(binding!!.password)
            valid = false
        } else {
            binding!!.password.error = null
        }
        return valid
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
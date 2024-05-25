package com.example.todo.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.MainActivity
import com.example.todo.R
import com.example.todo.adapter.ClickListener
import com.example.todo.adapter.RecyclerTouchListener
import com.example.todo.adapter.TodoAdapter
import com.example.todo.databinding.ActivityPanelBinding
import com.example.todo.model.Task
import com.example.todo.network.ApiTokenRestClient
import com.example.todo.util.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class PanelActivity : AppCompatActivity(), View.OnClickListener {
    var positionClicked = 0
    lateinit var progressDialog: ProgressDialog
    companion object {
        const val ADD_CODE = 100
        const val UPDATE_CODE = 200
        const val OK = 1
    }

    //ApiService apiService;
    lateinit var preferences: SharedPreferencesManager
    private lateinit var binding: ActivityPanelBinding
    private lateinit var adapter: TodoAdapter
    // private val listTasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml -> ActivityMainBinding
        binding = ActivityPanelBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        
        binding!!.floatingActionButton.setOnClickListener(this)
        preferences = SharedPreferencesManager(this)
        //showMessage("panel: " + preferences.getToken());
        
        initRecyclerView();

        //Destruir la instancia de Retrofit para que se cree una con el nuevo token
        ApiTokenRestClient.deleteInstance()
        downloadTasks()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        // super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh ->
                //petición al servidor para descargar de nuevo los sitios
                downloadTasks()
            R.id.email -> {
                //send an email
                val i = Intent(this, EmailActivity::class.java)
                startActivity(i)
            }
            R.id.exit -> {
                //petición al servidor para anular el token (a la ruta /api/logout)
                logout()

                preferences!!.saveToken(null, null)
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }
        return true
    }

    private fun initRecyclerView() {

        adapter = TodoAdapter()
        // binding.rvDogs.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding!!.recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                this,
                binding!!.recyclerView,
                object : ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        showMessage("Single Click on task with id: " + adapter!!.getAt(position).id)
                        modify(adapter!!.getAt(position))
                        positionClicked = position
                    }

                    override fun onLongClick(view: View?, position: Int) {
                        showMessage("Long press on position :$position")
                    }
                })
        )

        val itemSwipe = object : SimpleCallback(0, RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (binding.recyclerView.isEnabled) {
                    val position = viewHolder.adapterPosition
                    val id = adapter?.getId(position)
                    confirmDialog(id, position)
                } else {
                    binding.recyclerView.adapter?.notifyItemChanged(viewHolder.position)
                }
            }
        }
        val swap = ItemTouchHelper(itemSwipe)
        swap.attachToRecyclerView(binding.recyclerView)
    }


    override fun onClick(v: View) {
        if (v === binding!!.floatingActionButton) {
            val i = Intent(this, AddActivity::class.java)
            startActivityForResult(i, ADD_CODE)
        }
    }

    private fun downloadTasks() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setMessage("Connecting . . .")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()

        lifecycleScope.launch {
            try {
                val response = ApiTokenRestClient.getInstance(preferences!!.token)!!.getTasks()

                launch(Dispatchers.Main) {
                    progressDialog!!.dismiss()
                    if (response.isSuccessful) {
                        val getTasksResponse = response.body()
                        if (getTasksResponse!!.success!!) {
                            adapter!!.setTasks(getTasksResponse.data!!)
                            showMessage("Tasks downloaded ok")
                        } else {
                            showMessage("Error downloading the tasks: " + getTasksResponse.message)
                        }
                    } else {
                            val message = StringBuilder()
                            message.append("Error downloading the tasks: ")
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
                    // hideSoftKeyboard()
                    // progressDialog!!.dismiss()
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
        val task = Task()
        if (requestCode == ADD_CODE) if (resultCode == OK) {
            task.id = data!!.getIntExtra("id", 1).toLong()
            task.description = data.getStringExtra("description")
            task.createdAt = data.getStringExtra("createdAt")
            adapter!!.add(task)
        }
        if (requestCode == UPDATE_CODE) if (resultCode == OK) {
            task.id = data!!.getIntExtra("id", 1).toLong()
            task.description = data.getStringExtra("description")
            task.createdAt = data.getStringExtra("createdAt")
            adapter!!.modifyAt(task, positionClicked)
        }
    }
    private fun modify(task: Task) {
        var i = Intent(this, UpdateActivity::class.java)
        i.putExtra("task", task)
        startActivityForResult(i, UPDATE_CODE)
    }

    // private fun confirmDialog(idTask: Int, description: String, position: Int) {
    private fun confirmDialog(id: Long?, position: Int) {
        val builder = AlertDialog.Builder(this)
        val description = adapter.getAt(position).description
        builder.setMessage("$description\n Do you want to delete?")
            .setTitle("Delete")
            .setPositiveButton("Confirm") { dialog, which ->
                connection(id, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") {  dialog, which ->
                binding.recyclerView.adapter?.notifyItemChanged(position)
            }
        builder.show()
    }

    private fun connection(id: Long?, position: Int) {

        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setMessage("Connecting . . .")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()

        lifecycleScope.launch {
            try {
                val response = ApiTokenRestClient.getInstance(preferences!!.token)!!.deleteTask(id)

                launch(Dispatchers.Main) {
                    // hideSoftKeyboard()
                    progressDialog!!.dismiss()
                    if (response.isSuccessful) {
                        val deleteResponse = response.body()
                        if (deleteResponse!!.success!!) {
                            adapter!!.removeAt(position)
                            showMessage("Task deleted OK")
                        } else
                            showMessage("Error deleting the task")
                    } else {
                        val message = StringBuilder()
                        message.append("Error downloading the tasks: ")
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
                    // hideSoftKeyboard()
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

    private fun logout() {
        lifecycleScope.launch {
            try {
                val response = ApiTokenRestClient.getInstance(preferences!!.token)!!.logout()

                launch(Dispatchers.Main) {
                    progressDialog!!.dismiss()
                    if (response.isSuccessful) {
                        val logoutResponse = response.body()
                        if (logoutResponse!!.success!!) {
                            showMessage("Logout OK")
                        } else showMessage("Error in logout")
                    } else {
                        val message = StringBuilder()
                        message.append("Error in logout: ")
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
                    // hideSoftKeyboard()
                    // progressDialog!!.dismiss()
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
}
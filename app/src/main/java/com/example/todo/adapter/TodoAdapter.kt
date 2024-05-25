package com.example.todo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.ItemViewBinding
import com.example.todo.model.Task

class TodoAdapter : RecyclerView.Adapter<TodoAdapter.MyViewHolder>() {
    private var tasks: ArrayList<Task>

    init {
        tasks = ArrayList()
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class MyViewHolder(
        //Name of the item_view.xml in camel case + "Binding"
        val binding: ItemViewBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        )
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Get the data model based on position
        val task = tasks[position]
        holder.binding.id.text = task.id.toString()
        holder.binding.description.text = task.description
        holder.binding.createdAt.text = task.createdAt
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun getId(position: Int): Long {
        return tasks[position].id
    }

    fun setTasks(tasks: ArrayList<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    fun getAt(position: Int): Task {
        val task: Task
        task = tasks[position]
        return task
    }

    fun add(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size)
        notifyItemRangeChanged(0, tasks.size + 1)
    }

    fun modifyAt(task: Task, position: Int) {
        tasks[position] = task
        notifyItemChanged(position)
    }

    fun removeAt(position: Int) {
        tasks.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, tasks.size - 1)
    }
}
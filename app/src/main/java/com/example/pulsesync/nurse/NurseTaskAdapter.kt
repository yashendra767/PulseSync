package com.example.pulsesync.nurse

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.doctor.TaskItem
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class NurseTaskAdapter(private val tasks: List<TaskItem>) :
    RecyclerView.Adapter<NurseTaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textTaskTitle)
        val time: TextView = itemView.findViewById(R.id.textTaskTime)
        val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkBoxComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nurse_task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title

        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.time.text = task.timestamp?.let { sdf.format(it) } ?: task.time

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.completed
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            updateTaskCompletion(task.id, isChecked)
        }

        holder.title.paintFlags = if (task.completed) {
            holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount() = tasks.size

    private fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        FirebaseFirestore.getInstance().collection("tasks")
            .document(taskId)
            .update("completed", isCompleted, "completedBy", "nurse")
    }
}

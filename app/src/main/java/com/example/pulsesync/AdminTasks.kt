package com.example.pulsesync

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.doctor.TaskItem
import com.example.pulsesync.doctor.TaskAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AdminTasks : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val taskList = mutableListOf<TaskItem>()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var tvEmptyTasks: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewAdminTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TaskAdapter(taskList)
        recyclerView.adapter = adapter
        tvEmptyTasks = view.findViewById(R.id.tvEmptyTasks)


        view.findViewById<View>(R.id.fabAddTask).setOnClickListener {
            showAddTaskDialog()
        }

        fetchAllTasks()

        return view
    }

    private fun fetchAllTasks() {
        firestore.collection("tasks")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                taskList.clear()
                for (doc in documents) {
                    val task = doc.toObject(TaskItem::class.java).copy(id = doc.id)
                    taskList.add(task)
                }
                adapter.notifyDataSetChanged()
                tvEmptyTasks.animate().alpha(if (taskList.isEmpty()) 1f else 0f).setDuration(300).withEndAction {
                    tvEmptyTasks.visibility = if (taskList.isEmpty()) View.VISIBLE else View.GONE
                }.start()
            }
    }

    private fun showAddTaskDialog() {
        val firestore = FirebaseFirestore.getInstance()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_task_admin, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.inputTaskTitle)
        val timeInput = dialogView.findViewById<EditText>(R.id.inputTaskTime)
        val doctorSpinner = dialogView.findViewById<Spinner>(R.id.spinnerDoctor)
        val nurseSpinner = dialogView.findViewById<Spinner>(R.id.spinnerNurse)

        val doctorIds = mutableListOf<String>()
        val nurseIds = mutableListOf<String>()

        firestore.collection("users")
            .whereEqualTo("role", "Doctor")
            .get()
            .addOnSuccessListener { doctorSnapshot ->
                val doctorNames = doctorSnapshot.map {
                    doctorIds.add(it.id)
                    it.getString("name") ?: "Unnamed Doctor"
                }

                doctorSpinner.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    doctorNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                firestore.collection("users")
                    .whereEqualTo("role", "Nurse")
                    .get()
                    .addOnSuccessListener { nurseSnapshot ->
                        val nurseNames = nurseSnapshot.map {
                            nurseIds.add(it.id)
                            it.getString("name") ?: "Unnamed Nurse"
                        }

                        nurseSpinner.adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            nurseNames
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }

                        AlertDialog.Builder(requireContext())
                            .setTitle("Add Task")
                            .setView(dialogView)
                            .setPositiveButton("Add Task") { _, _ ->
                                val title = titleInput.text.toString()
                                val time = timeInput.text.toString()
                                val selectedDoctorId =
                                    doctorIds.getOrNull(doctorSpinner.selectedItemPosition) ?: ""
                                val selectedNurseId =
                                    nurseIds.getOrNull(nurseSpinner.selectedItemPosition) ?: ""

                                if (title.isBlank() || time.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Please fill all fields",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@setPositiveButton
                                }

                                val newTask = hashMapOf(
                                    "title" to title,
                                    "time" to time,
                                    "timestamp" to Date(),
                                    "completed" to false,
                                    "doctorId" to selectedDoctorId,
                                    "nurseId" to selectedNurseId
                                )

                                firestore.collection("tasks").add(newTask)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Task added", Toast.LENGTH_SHORT)
                                            .show()
                                        fetchAllTasks()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Failed to add task",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to load nurses", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load doctors", Toast.LENGTH_SHORT).show()
            }
    }
}

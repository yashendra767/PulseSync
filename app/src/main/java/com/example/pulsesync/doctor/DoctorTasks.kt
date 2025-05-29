package com.example.pulsesync.doctor

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.doctor.TaskAdapter
import com.example.pulsesync.doctor.TaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DoctorTasks : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val taskList = mutableListOf<TaskItem>()
    private val firestore = FirebaseFirestore.getInstance()
    private var showCompleted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_doctor_tasks, container, false)
        setHasOptionsMenu(true)
        recyclerView = view.findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TaskAdapter(taskList)
        recyclerView.adapter = adapter

        fetchTasks()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_doctor_tasks, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_tasks -> {
                showCompleted = !showCompleted
                item.title = if (showCompleted) "✅ Show Incomplete" else "📄 Show All"
                fetchTasks()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchTasks() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val doctorId = currentUser.uid

        var query = firestore.collection("tasks")
            .whereEqualTo("doctorId", doctorId)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        if (!showCompleted) {
            query = query.whereEqualTo("completed", false)
        }

        query.get()
            .addOnSuccessListener { documents ->
                taskList.clear()
                for (document in documents) {
                    val task = document.toObject(TaskItem::class.java).copy(id = document.id)
                    taskList.add(task)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading tasks", Toast.LENGTH_SHORT).show()
            }
    }
}

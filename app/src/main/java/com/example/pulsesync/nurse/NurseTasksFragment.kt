package com.example.pulsesync.nurse

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.doctor.TaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NurseTasksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: NurseTaskAdapter
    private val taskList = mutableListOf<TaskItem>()
    private var currentFilter = "All"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_nurse_tasks, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewNurseTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = NurseTaskAdapter(taskList)
        recyclerView.adapter = taskAdapter
        fetchNurseTasks()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.nurse_task_filter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        currentFilter = when (item.itemId) {
            R.id.filter_all -> "All"
            R.id.filter_pending -> "Pending"
            R.id.filter_completed -> "Completed"
            else -> "All"
        }
        fetchNurseTasks()
        return true
    }

    private fun fetchNurseTasks() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val nurseId = currentUser.uid
        var query = FirebaseFirestore.getInstance().collection("tasks")
            .whereEqualTo("nurseId", nurseId)

        when (currentFilter) {
            "Completed" -> query = query.whereEqualTo("completed", true)
            "Pending" -> query = query.whereEqualTo("completed", false)
        }

        query.orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                taskList.clear()
                for (document in querySnapshot) {
                    val task = TaskItem(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        time = document.getString("time") ?: "",
                        nurseId = document.getString("nurseId") ?: "",
                        timestamp = document.getDate("timestamp"),
                        doctorId = "",
                        completed = document.getBoolean("completed") ?: false
                    )
                    taskList.add(task)
                }
                taskAdapter.notifyDataSetChanged()
            }
    }
}

package com.example.pulsesync.beds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.google.firebase.firestore.FirebaseFirestore

class Admission : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdmissionAdapter
    private val admissionsList = mutableListOf<AdmissionItem>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_admission, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewAdmissions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AdmissionAdapter(admissionsList) { admissionItem ->
            deleteAdmission(admissionItem)
        }
        recyclerView.adapter = adapter

        setupSwipeToEdit()
        fetchAdmissions()
        return view
    }

    fun fetchAdmissions() {
        firestore.collection("admissions").get()
            .addOnSuccessListener { documents ->
                admissionsList.clear()
                for (document in documents) {
                    val admission = document.toObject(AdmissionItem::class.java)
                        .copy(id = document.id)
                    admissionsList.add(admission)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching admissions", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAdmission(admissionItem: AdmissionItem) {
        firestore.collection("admissions").document(admissionItem.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Admission deleted", Toast.LENGTH_SHORT).show()
                fetchAdmissions()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error deleting admission", Toast.LENGTH_SHORT).show()
            }
    }
    private fun setupSwipeToEdit() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val admission = admissionsList[position]

                val bottomSheet = AddAdmissionBottomSheet(object : AddAdmissionBottomSheet.OnAdmissionAddedListener {
                    override fun onAdmissionAdded() {
                        fetchAdmissions()
                    }
                }, existingAdmission = admission)

                bottomSheet.show(parentFragmentManager, "EditAdmission")
                adapter.notifyItemChanged(position)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}


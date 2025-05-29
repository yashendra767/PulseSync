package com.example.pulsesync.beds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.google.firebase.firestore.FirebaseFirestore

class Beds : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BedsAdapter
    private val bedsList = mutableListOf<BedItem>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_beds, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewBeds)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BedsAdapter(bedsList)
        recyclerView.adapter = adapter

        fetchBeds()

        return view
    }

    fun fetchBeds() {
        firestore.collection("beds").get()
            .addOnSuccessListener { documents ->
                bedsList.clear()
                for (document in documents) {
                    val bed = document.toObject(BedItem::class.java)
                    bedsList.add(bed)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching beds", Toast.LENGTH_SHORT).show()
            }
    }
}

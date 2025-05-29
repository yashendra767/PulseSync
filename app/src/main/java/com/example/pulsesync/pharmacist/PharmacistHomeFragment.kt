package com.example.pulsesync.pharmacist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.inventory.InventoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PharmacistHomeFragment : Fragment() {

    private lateinit var tvLowStockCount: TextView
    private lateinit var tvTotalDispensed: TextView
    private lateinit var recyclerLowStock: RecyclerView

    private val lowStockItems = mutableListOf<InventoryItem>()
    private lateinit var adapter: LowStockAdapter
    private val LOW_STOCK_THRESHOLD = 5
    private lateinit var tvNoLowStock: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pharmacist_home, container, false)
        tvLowStockCount = view.findViewById(R.id.tvLowStockCount)
        tvTotalDispensed = view.findViewById(R.id.tvTotalDispensed)
        recyclerLowStock = view.findViewById(R.id.recyclerLowStock)
        tvNoLowStock = view.findViewById(R.id.tv_no_low_stock)

        val helloText = view.findViewById<TextView>(R.id.helloText)
        val db : FirebaseFirestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener {
                    doc->
                val name = doc.getString("name") ?: "Pharma"
                helloText.text = "Hello $name"
            }

        adapter = LowStockAdapter(lowStockItems)
        recyclerLowStock.layoutManager = LinearLayoutManager(requireContext())
        recyclerLowStock.adapter = adapter

        fetchLowStockItems()
        fetchTotalDispensed()

        return view
    }

    private fun fetchLowStockItems() {
        FirebaseFirestore.getInstance()
            .collection("inventory")
            .whereLessThanOrEqualTo("quantity", LOW_STOCK_THRESHOLD)
            .get()
            .addOnSuccessListener { snapshot ->
                lowStockItems.clear()
                for (doc in snapshot) {
                    val item = doc.toObject(InventoryItem::class.java)
                    lowStockItems.add(item)
                }
                adapter.notifyDataSetChanged()
                tvLowStockCount.text = "Low stock items: ${lowStockItems.size}"
                tvNoLowStock.visibility = if (lowStockItems.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                tvLowStockCount.text = "Failed to load low stock items"
                tvNoLowStock.visibility = View.VISIBLE
            }
    }

    private fun fetchTotalDispensed() {
        FirebaseFirestore.getInstance()
            .collection("dispensing_history")
            .get()
            .addOnSuccessListener { snapshot ->
                var totalDispensed = 0
                for (doc in snapshot) {
                    val qty = doc.getLong("quantityDispensed") ?: 0
                    totalDispensed += qty.toInt()
                }
                tvTotalDispensed.text = "Total items dispensed: $totalDispensed"
            }
            .addOnFailureListener {
                tvTotalDispensed.text = "Failed to load dispense data"
            }
    }
}

package com.example.pulsesync.pharmacist

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pulsesync.R
import com.example.pulsesync.databinding.ActivityDispensingHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore

class DispensingHistory : AppCompatActivity() {

    private lateinit var binding: ActivityDispensingHistoryBinding
    private lateinit var adapter: DispenseHistoryAdapter
    private val historyList = mutableListOf<DispenseHistoryItem>()
    private val filteredList = mutableListOf<DispenseHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispensingHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        val toolbarDispense = findViewById<Toolbar>(R.id.toolbarDispense)
        toolbarDispense.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = DispenseHistoryAdapter(historyList)
        binding.recyclerViewDispenseHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDispenseHistory.adapter = adapter

        setupSearchView()
        fetchDispenseHistory()
    }

    private fun fetchDispenseHistory() {
        FirebaseFirestore.getInstance()
            .collection("dispensing_history")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                historyList.clear()
                for (doc in snapshot) {
                    val item = doc.toObject(DispenseHistoryItem::class.java)
                    historyList.add(item)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Dispense History not Loaded", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText.orEmpty())
                return true
            }
        })
    }

    private fun filter(query: String) {
        val lowerQuery = query.lowercase()
        filteredList.clear()
        filteredList.addAll(
            historyList.filter {
                it.itemName.lowercase().contains(lowerQuery) ||
                        it.note.lowercase().contains(lowerQuery)
            }
        )
        adapter.notifyDataSetChanged()
    }
}

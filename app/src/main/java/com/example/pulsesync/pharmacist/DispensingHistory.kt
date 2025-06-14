package com.example.pulsesync.pharmacist

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
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
                if (!snapshot.isEmpty) {
                    for (doc in snapshot) {
                        val item = DispenseHistoryItem(
                            dispensedBy = doc.getString("dispensedBy") ?: "",
                            dispensedTo = doc.getString("dispensedTo") ?: "",
                            itemId = doc.getString("itemId") ?: "",
                            itemName = doc.getString("itemName") ?: "",
                            quantityDispensed = (doc.getLong("quantityDispensed") ?: 0L).toInt(),
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            note = doc.getString("note") ?: ""
                        )
                        Log.d("DISPENSE", "Parsed manually: ${item.itemName}, ${item.timestamp}")
                        historyList.add(item)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Dispense History not Loaded", Toast.LENGTH_SHORT).show()
            }
    }



    private fun setupSearchView() {
        binding.searchView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.searchView.text.isNullOrBlank()) {
                adapter.updateList(historyList)
            }
        }

        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                if (query.isEmpty()) {
                    adapter.updateList(historyList)
                } else {
                    val filtered = historyList.filter {
                        it.itemName.contains(query, ignoreCase = true) ||
                                it.note.contains(query, ignoreCase = true)
                    }
                    adapter.updateList(filtered)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }



    private fun filter(query: String) {
        val lowerQuery = query.lowercase()
        val filtered = historyList.filter {
            it.itemName.lowercase().contains(lowerQuery) ||
                    it.note.lowercase().contains(lowerQuery)
        }
        adapter.updateList(filtered)
    }
}

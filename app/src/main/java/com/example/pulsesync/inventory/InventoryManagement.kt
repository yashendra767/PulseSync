package com.example.pulsesync.inventory

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton

class InventoryManagement : AppCompatActivity() {
    private val lowStockThreshold = 5
    private val sharedPrefs by lazy {
        getSharedPreferences("LowStockPrefs", MODE_PRIVATE)
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var searchEditText: EditText
    private lateinit var addItemFAB: FloatingActionButton
    private lateinit var categoryTabs: TabLayout

    private val inventoryList = mutableListOf<InventoryItem>()
    private val filteredList = mutableListOf<InventoryItem>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_management)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        recyclerView = findViewById(R.id.inventoryRecyclerView)
        searchEditText = findViewById(R.id.searchInventoryEditText)
        addItemFAB = findViewById(R.id.addInventoryFAB)
        categoryTabs = findViewById(R.id.categoryTabs)

        val toolbarInventory = findViewById<Toolbar>(R.id.inventoryToolbar)
        toolbarInventory.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        inventoryAdapter = InventoryAdapter(this, filteredList) {
            fetchInventoryFromFirestore()
        }
        recyclerView.adapter = inventoryAdapter

        fetchInventoryFromFirestore()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterInventory(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        categoryTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterByCategory(tab?.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        addItemFAB.setOnClickListener {
            checkUserRoleAndOpenBottomSheet()
        }
    }

    private fun checkUserRoleAndOpenBottomSheet() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "Admin") {
                        openAddInventoryBottomSheet()
                    } else {
                        Toast.makeText(this, "Only admins can add inventory items", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking user role", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openAddInventoryBottomSheet() {
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_inventory, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        val etItemName = bottomSheetView.findViewById<TextInputEditText>(R.id.etItemName)
        val etQuantity = bottomSheetView.findViewById<TextInputEditText>(R.id.etItemQuantity)
        val etCategory = bottomSheetView.findViewById<TextInputEditText>(R.id.etItemCategory)
        val etExpiryDate = bottomSheetView.findViewById<TextInputEditText>(R.id.etItemExpiry)
        val btnAdd = bottomSheetView.findViewById<MaterialButton>(R.id.btnAddItem)

        btnAdd.setOnClickListener {
            val name = etItemName.text.toString().trim()
            val quantityStr = etQuantity.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val expiry = etExpiryDate.text.toString().trim()

            if (name.isEmpty() || quantityStr.isEmpty() || category.isEmpty() || expiry.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityStr.toIntOrNull()
            if (quantity == null) {
                Toast.makeText(this, "Quantity must be a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newItemRef = firestore.collection("inventory").document()
            val newItem = InventoryItem(
                uid = newItemRef.id,
                itemName = name,
                quantity = quantity,
                category = category,
                expiryDate = expiry
            )

            newItemRef.set(newItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                    fetchInventoryFromFirestore()
                    bottomSheetDialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
        }

        bottomSheetDialog.show()
    }

    private fun fetchInventoryFromFirestore() {
        firestore.collection("inventory")
            .get()
            .addOnSuccessListener { documents ->
                inventoryList.clear()
                for (document in documents) {
                    val item = document.toObject(InventoryItem::class.java)
                    inventoryList.add(item)
                }
                applyCurrentFilters()
                clearNotifiedItemsIfRestocked()
                checkAndNotifyLowStockItems()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch inventory", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterInventory(query: String) {
        val lowerQuery = query.lowercase()
        filteredList.clear()
        filteredList.addAll(inventoryList.filter {
            it.itemName.lowercase().contains(lowerQuery) || it.category.lowercase().contains(lowerQuery)
        })
        inventoryAdapter.notifyDataSetChanged()
    }

    private fun filterByCategory(category: String) {
        if (category == "All") {
            applyCurrentFilters()
            return
        }
        filteredList.clear()
        filteredList.addAll(inventoryList.filter { it.category.equals(category, ignoreCase = true) })
        inventoryAdapter.notifyDataSetChanged()
    }

    private fun applyCurrentFilters() {
        val selectedTab = categoryTabs.getTabAt(categoryTabs.selectedTabPosition)?.text.toString()
        filteredList.clear()

        if (selectedTab == "All") {
            filteredList.addAll(inventoryList)
        } else {
            filteredList.addAll(inventoryList.filter { it.category.equals(selectedTab, ignoreCase = true) })
        }

        inventoryAdapter.notifyDataSetChanged()
    }
    private fun checkAndNotifyLowStockItems() {
        val notifiedSet = sharedPrefs.getStringSet("notified_items", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        for (item in inventoryList) {
            if (item.quantity <= lowStockThreshold && !notifiedSet.contains(item.uid)) {
                sendLowStockNotification(item.itemName, item.quantity)
                notifiedSet.add(item.uid)
            }
        }
        sharedPrefs.edit { putStringSet("notified_items", notifiedSet) }
    }

    private fun sendLowStockNotification(itemName: String, quantity: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "low_stock_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Low Stock Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.warning)
            .setContentTitle("Low Stock Alert")
            .setContentText("$itemName is running low! Only $quantity left.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(itemName.hashCode(), notification)
    }
    private fun clearNotifiedItemsIfRestocked() {
        val notifiedSet = sharedPrefs.getStringSet("notified_items", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val updatedSet = notifiedSet.filterNot { uid ->
            val item = inventoryList.find { it.uid == uid }
            item != null && item.quantity > lowStockThreshold
        }.toMutableSet()
        sharedPrefs.edit { putStringSet("notified_items", updatedSet) }
    }

}

package com.example.pulsesync.inventory

data class InventoryItem(
    val uid: String = "",
    val itemName: String = "",
    val quantity: Int = 0,
    val category: String = "",
    val expiryDate: String = ""
)

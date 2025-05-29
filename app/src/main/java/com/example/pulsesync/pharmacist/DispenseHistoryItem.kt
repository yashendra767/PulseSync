package com.example.pulsesync.pharmacist

data class DispenseHistoryItem(
    val itemId: String = "",
    val itemName: String = "",
    val quantityDispensed: Int = 0,
    val timestamp: Long = 0L,
    val note: String = ""
)
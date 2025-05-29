package com.example.pulsesync.doctor

data class AlertItem(
    val title: String ="",
    val description: String="",
    val severity: String = "Low",
    val bedId: String = "",
    val timestamp: Long = 0L
)
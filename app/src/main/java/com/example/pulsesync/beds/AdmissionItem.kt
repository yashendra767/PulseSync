package com.example.pulsesync.beds

data class AdmissionItem(
    val id: String = "",
    val patientName: String = "",
    val bedId: String = "",
    val date: String = "",
    val status: String = "",
    val temperature: Double = 0.0,
    val oxygen: Double = 0.0,
    val bloodPressure: String = "",
    val lastUpdated: Long = 0L
)

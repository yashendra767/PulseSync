package com.example.pulsesync.reports

import com.google.firebase.Timestamp

data class Report(
    val title: String = "",
    val period: String = "",
    val summary: String = "",
    val type: String = "",
    val date: Timestamp? = null
)

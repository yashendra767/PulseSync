package com.example.pulsesync.doctor

import java.util.Date

data class TaskItem(
    val id: String = "",
    val title: String = "",
    val time: String = "",
    val timestamp: Date? = null,
    val completed: Boolean = false,
    val doctorId: String = "",
    val nurseId: String = ""
)

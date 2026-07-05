package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labour_logs")
data class LabourLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val workerName: String,
    val date: String, // Format: YYYY-MM-DD
    val status: String, // "Present", "Absent", "Half Day"
    val hoursWorked: Double,
    val amountEarned: Double,
    val paymentStatus: String, // "Paid", "Unpaid"
    val notes: String = "",
    val synced: Boolean = false
)

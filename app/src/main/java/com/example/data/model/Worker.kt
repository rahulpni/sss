package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // e.g., Mason, Helper, Carpenter, Supervisor
    val dailyRate: Double,
    val phoneNumber: String = "",
    val isActive: Boolean = true
)

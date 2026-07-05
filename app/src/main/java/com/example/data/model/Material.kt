package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class Material(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g. Structural, Finishing, Plumbing, Electrical
    val unit: String, // e.g. Bags, Tons, Cum, Pieces, Liters
    val currentStock: Double,
    val minimumRequired: Double = 0.0
)

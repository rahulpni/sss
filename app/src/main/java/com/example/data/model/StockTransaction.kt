package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materialId: Int,
    val materialName: String,
    val type: String, // "IN" (Incoming stock delivery), "OUT" (Site usage/outgoing)
    val quantity: Double,
    val date: String, // Format: YYYY-MM-DD
    val reference: String = "", // e.g. Supplier Invoice #, Task reference
    val recordedBy: String = "", // e.g. Name of supervisor
    val synced: Boolean = false
)

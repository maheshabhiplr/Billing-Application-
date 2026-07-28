package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,             // E.g., സുരേഷ് കുമാർ (Suresh Kumar)
    val phone: String = "",
    val address: String = "",
    val previousBalance: Double = 0.0, // Existing outstanding balance
    val createdAt: Long = System.currentTimeMillis()
)

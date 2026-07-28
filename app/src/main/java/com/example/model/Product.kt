package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameMalayalam: String, // E.g., വെളിച്ചെണ്ണ
    val nameEnglish: String = "", // E.g., Coconut Oil
    val unit: String = "kg",    // "kg", "pcs", "litre", "packet"
    val pricePerUnit: Double,   // Price per Kg or per Pcs
    val category: String = "പലവ്യഞ്ജനം", // Grocery category
    val updatedAt: Long = System.currentTimeMillis()
)

package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,      // E.g., INV-1001
    val customerId: Long? = null,
    val customerName: String,       // E.g., രമേഷൻ (Rameshan)
    val customerPhone: String = "",
    val dateTimestamp: Long = System.currentTimeMillis(),
    val totalItemCount: Int = 0,    // Total distinct line items count
    val totalWeightKg: Double = 0.0,// Total combined weight in kg
    val subTotal: Double,           // Sum of items
    val previousBalanceAdded: Double = 0.0, // Included previous balance
    val discount: Double = 0.0,
    val grandTotal: Double,         // SubTotal + PreviousBalance - Discount
    val amountPaid: Double = 0.0,   // Amount paid now
    val dueBalance: Double = 0.0,   // GrandTotal - AmountPaid
    val notes: String = ""
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val productId: Long? = null,
    val productName: String,        // E.g. അരി (Rice)
    val unit: String = "kg",        // "kg" or "pcs"
    val quantityKg: Double = 0.0,   // E.g. 2.0
    val quantityGrm: Double = 0.0,  // E.g. 500.0 (Grams)
    val unitPrice: Double,          // Price per Kg or Pcs
    val totalPrice: Double          // (Kg + Grm/1000) * unitPrice
)

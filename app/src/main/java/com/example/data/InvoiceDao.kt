package com.example.data

import androidx.room.*
import com.example.model.Invoice
import com.example.model.InvoiceItem
import kotlinx.coroutines.flow.Flow

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItem>
)

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY dateTimestamp DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Transaction
    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY dateTimestamp DESC")
    fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItems(invoiceId: Long)

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoiceCount(): Int
}

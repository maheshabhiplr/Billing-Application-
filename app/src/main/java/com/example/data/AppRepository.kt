package com.example.data

import com.example.model.CompanyDetails
import com.example.model.Customer
import com.example.model.Invoice
import com.example.model.InvoiceItem
import com.example.model.Product
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val customerDao: CustomerDao,
    private val productDao: ProductDao,
    private val invoiceDao: InvoiceDao,
    private val companyDao: CompanyDao
) {
    // Customers
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    suspend fun saveCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomerBalance(customerId: Long, newBalance: Double) =
        customerDao.updateCustomerBalance(customerId, newBalance)

    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    suspend fun saveProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProductPrice(productId: Long, newPrice: Double) =
        productDao.updateProductPrice(productId, newPrice)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    // Invoices
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems? =
        invoiceDao.getInvoiceWithItemsById(id)

    fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceWithItems>> =
        invoiceDao.getInvoicesForCustomer(customerId)

    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = invoiceDao.insertInvoice(invoice)
        val itemsWithInvoiceId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceDao.insertInvoiceItems(itemsWithInvoiceId)

        // If customer is selected and invoice has due balance or previous balance, update customer's balance!
        if (invoice.customerId != null && invoice.customerId > 0) {
            val customer = customerDao.getCustomerById(invoice.customerId)
            if (customer != null) {
                // New Balance = Previous balance included/remaining + new unpaid due
                val updatedBalance = invoice.dueBalance
                customerDao.updateCustomerBalance(invoice.customerId, updatedBalance)
            }
        }
        return invoiceId
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.deleteInvoiceItems(invoice.id)
        invoiceDao.deleteInvoice(invoice)
    }

    suspend fun getNextInvoiceNumber(): String {
        val count = invoiceDao.getInvoiceCount() + 1
        return "INV-${System.currentTimeMillis() % 100000}-$count"
    }

    // Company Details
    val companyDetails: Flow<CompanyDetails?> = companyDao.getCompanyDetails()

    suspend fun getCompanyDetailsOnce(): CompanyDetails? = companyDao.getCompanyDetailsOnce()

    suspend fun saveCompanyDetails(companyDetails: CompanyDetails) =
        companyDao.insertOrUpdateCompanyDetails(companyDetails)
}

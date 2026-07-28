package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.InvoiceWithItems
import com.example.model.CompanyDetails
import com.example.model.Customer
import com.example.model.Invoice
import com.example.model.InvoiceItem
import com.example.model.Product
import com.example.util.IndianCurrencyUtils
import com.example.util.PdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class DraftItem(
    val product: Product,
    val quantityKg: Double = 1.0,
    val quantityGrm: Double = 0.0,
    val unitPrice: Double = product.pricePerUnit
) {
    val totalWeightKg: Double
        get() = if (product.unit == "kg") quantityKg + (quantityGrm / 1000.0) else quantityKg

    val totalPrice: Double
        get() = totalWeightKg * unitPrice
}

data class DraftInvoiceState(
    val customer: Customer? = null,
    val customCustomerName: String = "",
    val customCustomerPhone: String = "",
    val items: List<DraftItem> = emptyList(),
    val previousBalanceAdded: Double = 0.0,
    val discount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val notes: String = ""
) {
    val subTotal: Double
        get() = items.sumOf { it.totalPrice }

    val totalItemCount: Int
        get() = items.size

    val totalWeightKg: Double
        get() = items.filter { it.product.unit == "kg" }.sumOf { it.totalWeightKg }

    val grandTotal: Double
        get() = (subTotal + previousBalanceAdded - discount).coerceAtLeast(0.0)

    val dueBalance: Double
        get() = (grandTotal - amountPaid).coerceAtLeast(0.0)

    val grandTotalInFigures: String
        get() = IndianCurrencyUtils.formatToIndianRupees(grandTotal)

    val grandTotalInWordsEng: String
        get() = IndianCurrencyUtils.convertToIndianRupeesInWords(grandTotal)

    val grandTotalInWordsMl: String
        get() = IndianCurrencyUtils.convertToMalayalamRupeesInWords(grandTotal)
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        customerDao = db.customerDao(),
        productDao = db.productDao(),
        invoiceDao = db.invoiceDao(),
        companyDao = db.companyDao()
    )

    // Data Flows
    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companyDetails: StateFlow<CompanyDetails?> = repository.companyDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Draft Invoice State
    private val _draftInvoice = MutableStateFlow(DraftInvoiceState())
    val draftInvoice: StateFlow<DraftInvoiceState> = _draftInvoice.asStateFlow()

    // Selected Invoice for Details / PDF view
    private val _selectedInvoiceDetails = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoiceDetails: StateFlow<InvoiceWithItems?> = _selectedInvoiceDetails.asStateFlow()

    // Draft Invoice Mutations
    fun selectCustomerForBill(customer: Customer?) {
        _draftInvoice.update { current ->
            current.copy(
                customer = customer,
                customCustomerName = customer?.name ?: "",
                customCustomerPhone = customer?.phone ?: "",
                previousBalanceAdded = customer?.previousBalance ?: 0.0
            )
        }
    }

    fun setCustomCustomerDetails(name: String, phone: String) {
        _draftInvoice.update { current ->
            current.copy(
                customCustomerName = name,
                customCustomerPhone = phone
            )
        }
    }

    fun addItemToBill(product: Product, kg: Double = 1.0, grm: Double = 0.0, price: Double = product.pricePerUnit) {
        _draftInvoice.update { current ->
            val existingIndex = current.items.indexOfFirst { it.product.id == product.id && product.id > 0 }
            if (existingIndex >= 0) {
                val existing = current.items[existingIndex]
                val updatedItem = existing.copy(
                    quantityKg = existing.quantityKg + kg,
                    quantityGrm = existing.quantityGrm + grm,
                    unitPrice = price
                )
                val newList = current.items.toMutableList()
                newList[existingIndex] = updatedItem
                current.copy(items = newList)
            } else {
                val newItem = DraftItem(product, kg, grm, price)
                current.copy(items = current.items + newItem)
            }
        }
    }

    fun updateBillItem(index: Int, kg: Double, grm: Double, price: Double) {
        _draftInvoice.update { current ->
            if (index in current.items.indices) {
                val newList = current.items.toMutableList()
                newList[index] = newList[index].copy(
                    quantityKg = kg.coerceAtLeast(0.0),
                    quantityGrm = grm.coerceAtLeast(0.0),
                    unitPrice = price.coerceAtLeast(0.0)
                )
                current.copy(items = newList)
            } else current
        }
    }

    fun removeItemFromBill(index: Int) {
        _draftInvoice.update { current ->
            if (index in current.items.indices) {
                val newList = current.items.toMutableList()
                newList.removeAt(index)
                current.copy(items = newList)
            } else current
        }
    }

    fun setPreviousBalance(amount: Double) {
        _draftInvoice.update { it.copy(previousBalanceAdded = amount.coerceAtLeast(0.0)) }
    }

    fun setDiscount(amount: Double) {
        _draftInvoice.update { it.copy(discount = amount.coerceAtLeast(0.0)) }
    }

    fun setPaidAmount(amount: Double) {
        _draftInvoice.update { it.copy(amountPaid = amount.coerceAtLeast(0.0)) }
    }

    fun clearDraftBill() {
        _draftInvoice.value = DraftInvoiceState()
    }

    fun saveInvoiceAndExportPdf(context: Context, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val draft = _draftInvoice.value
            if (draft.items.isEmpty()) {
                onComplete(null)
                return@launch
            }

            val custName = if (draft.customer != null) draft.customer.name else (draft.customCustomerName.ifBlank { "റോക്കറ്റ് ഉപഭോക്താവ് (Cash Customer)" })
            val custPhone = if (draft.customer != null) draft.customer.phone else draft.customCustomerPhone

            val invNumber = repository.getNextInvoiceNumber()

            val newInvoice = Invoice(
                invoiceNumber = invNumber,
                customerId = draft.customer?.id,
                customerName = custName,
                customerPhone = custPhone,
                totalItemCount = draft.totalItemCount,
                totalWeightKg = draft.totalWeightKg,
                subTotal = draft.subTotal,
                previousBalanceAdded = draft.previousBalanceAdded,
                discount = draft.discount,
                grandTotal = draft.grandTotal,
                amountPaid = draft.amountPaid,
                dueBalance = draft.dueBalance,
                notes = draft.notes
            )

            val invoiceItems = draft.items.map { draftItem ->
                InvoiceItem(
                    invoiceId = 0, // Will be auto-assigned by DAO
                    productId = draftItem.product.id,
                    productName = draftItem.product.nameMalayalam,
                    unit = draftItem.product.unit,
                    quantityKg = draftItem.quantityKg,
                    quantityGrm = draftItem.quantityGrm,
                    unitPrice = draftItem.unitPrice,
                    totalPrice = draftItem.totalPrice
                )
            }

            val invoiceId = repository.saveInvoice(newInvoice, invoiceItems)
            val savedInvoiceWithItems = repository.getInvoiceWithItemsById(invoiceId)

            if (savedInvoiceWithItems != null) {
                val company = repository.getCompanyDetailsOnce()
                val pdfFile = PdfGenerator.generateInvoicePdf(context, savedInvoiceWithItems, company)
                clearDraftBill()
                onComplete(pdfFile)
            } else {
                onComplete(null)
            }
        }
    }

    // Daily Price Updates
    fun updateProductPrice(productId: Long, newPrice: Double) {
        viewModelScope.launch {
            repository.updateProductPrice(productId, newPrice)
        }
    }

    // Customer operations
    fun addCustomer(name: String, phone: String, address: String, initialBalance: Double) {
        viewModelScope.launch {
            val customer = Customer(
                name = name,
                phone = phone,
                address = address,
                previousBalance = initialBalance
            )
            repository.saveCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Product operations
    fun addProduct(nameMl: String, nameEng: String, unit: String, price: Double, category: String) {
        viewModelScope.launch {
            val product = Product(
                nameMalayalam = nameMl,
                nameEnglish = nameEng,
                unit = unit,
                pricePerUnit = price,
                category = category
            )
            repository.saveProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Company operations
    fun updateCompanyDetails(company: CompanyDetails) {
        viewModelScope.launch {
            repository.saveCompanyDetails(company)
        }
    }

    fun loadInvoiceDetails(invoiceId: Long) {
        viewModelScope.launch {
            val inv = repository.getInvoiceWithItemsById(invoiceId)
            _selectedInvoiceDetails.value = inv
        }
    }
}

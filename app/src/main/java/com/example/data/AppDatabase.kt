package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.CompanyDetails
import com.example.model.Customer
import com.example.model.Invoice
import com.example.model.InvoiceItem
import com.example.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Customer::class,
        Product::class,
        Invoice::class,
        InvoiceItem::class,
        CompanyDetails::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun companyDao(): CompanyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "malayalam_store_billing.db"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                // Populate default company
                db.companyDao().insertOrUpdateCompanyDetails(
                    CompanyDetails(
                        id = 1,
                        companyNameMalayalam = "എസ്.കെ. ട്രേഡേഴ്സ് & പ്രൊവിഷൻസ്",
                        companyNameEnglish = "SK Traders & Provisions",
                        tagline = "മൊത്ത-ചില്ലറ വ്യാപാരി (Wholesale & Retail Merchant)",
                        address = "മെയിൻ റോഡ്, ആലുവ, എറണാകുളം, കേരളം - 683101",
                        phone = "+91 98470 12345",
                        gstin = "32ABCDE1234F1Z5",
                        upiId = "sktraders@upi"
                    )
                )

                // Populate default Malayalam products
                val initialProducts = listOf(
                    Product(nameMalayalam = "മട്ട അരി", nameEnglish = "Matta Rice", unit = "kg", pricePerUnit = 52.0, category = "ധാന്യങ്ങൾ"),
                    Product(nameMalayalam = "ജയ അരി", nameEnglish = "Jaya Rice", unit = "kg", pricePerUnit = 48.0, category = "ധാന്യങ്ങൾ"),
                    Product(nameMalayalam = "വെളിച്ചെണ്ണ", nameEnglish = "Coconut Oil", unit = "kg", pricePerUnit = 185.0, category = "എണ്ണകൾ"),
                    Product(nameMalayalam = "പഞ്ചസാര", nameEnglish = "Sugar", unit = "kg", pricePerUnit = 44.0, category = "പലവ്യഞ്ജനം"),
                    Product(nameMalayalam = "ചെറുപയർ", nameEnglish = "Green Gram", unit = "kg", pricePerUnit = 120.0, category = "പയറുവർഗ്ഗങ്ങൾ"),
                    Product(nameMalayalam = "കടല", nameEnglish = "Chickpeas", unit = "kg", pricePerUnit = 95.0, category = "പയറുവർഗ്ഗങ്ങൾ"),
                    Product(nameMalayalam = "ഉഴുന്ന്", nameEnglish = "Urad Dal", unit = "kg", pricePerUnit = 130.0, category = "പയറുവർഗ്ഗങ്ങൾ"),
                    Product(nameMalayalam = "തുവരപ്പരിപ്പ്", nameEnglish = "Toor Dal", unit = "kg", pricePerUnit = 160.0, category = "പയറുവർഗ്ഗങ്ങൾ"),
                    Product(nameMalayalam = "ആട്ട (ഗോതമ്പ്)", nameEnglish = "Atta Wheat Flour", unit = "kg", pricePerUnit = 46.0, category = "പൊടികൾ"),
                    Product(nameMalayalam = "മൈദ", nameEnglish = "Maida", unit = "kg", pricePerUnit = 42.0, category = "പൊടികൾ"),
                    Product(nameMalayalam = "റവ", nameEnglish = "Rava", unit = "kg", pricePerUnit = 48.0, category = "പൊടികൾ"),
                    Product(nameMalayalam = "തേയില", nameEnglish = "Tea Powder", unit = "kg", pricePerUnit = 280.0, category = "പാനീയങ്ങൾ"),
                    Product(nameMalayalam = "കാപ്പിപ്പൊടി", nameEnglish = "Coffee Powder", unit = "kg", pricePerUnit = 420.0, category = "പാനീയങ്ങൾ"),
                    Product(nameMalayalam = "മുളകുപൊടി", nameEnglish = "Chilli Powder", unit = "kg", pricePerUnit = 240.0, category = "മസാലപ്പൊടികൾ"),
                    Product(nameMalayalam = "മഞ്ഞൾപ്പൊടി", nameEnglish = "Turmeric Powder", unit = "kg", pricePerUnit = 190.0, category = "മസാലപ്പൊടികൾ"),
                    Product(nameMalayalam = "മല്ലിപ്പൊടി", nameEnglish = "Coriander Powder", unit = "kg", pricePerUnit = 210.0, category = "മസാലപ്പൊടികൾ"),
                    Product(nameMalayalam = "കുരുമുളക്", nameEnglish = "Black Pepper", unit = "kg", pricePerUnit = 620.0, category = "സുഗന്ധവ്യഞ്ജനങ്ങൾ"),
                    Product(nameMalayalam = "ജീരകം", nameEnglish = "Cumin Seeds", unit = "kg", pricePerUnit = 380.0, category = "സുഗന്ധവ്യഞ്ജനങ്ങൾ"),
                    Product(nameMalayalam = "കടുക്", nameEnglish = "Mustard Seeds", unit = "kg", pricePerUnit = 110.0, category = "സുഗന്ധവ്യഞ്ജനങ്ങൾ"),
                    Product(nameMalayalam = "ഉപ്പ്", nameEnglish = "Salt", unit = "kg", pricePerUnit = 20.0, category = "പലവ്യഞ്ജനം")
                )
                db.productDao().insertProducts(initialProducts)

                // Populate default customers
                val initialCustomers = listOf(
                    Customer(name = "ബിജു കെ.പി. (Biju K.P.)", phone = "9846011223", address = "ആലുവ, എറണാകുളം", previousBalance = 250.0),
                    Customer(name = "ഷാജി മാത്യു (Shaji Mathew)", phone = "9447122334", address = "പെരുമ്പാവൂർ, എറണാകുളം", previousBalance = 1200.0),
                    Customer(name = "അബ്ദുൾ റഹ്മാൻ (Abdul Rahman)", phone = "9895033445", address = "കളമശ്ശേരി, എറണാകുളം", previousBalance = 0.0)
                )
                for (customer in initialCustomers) {
                    db.customerDao().insertCustomer(customer)
                }
            }
        }
    }
}

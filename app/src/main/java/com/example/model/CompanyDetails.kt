package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_details")
data class CompanyDetails(
    @PrimaryKey
    val id: Int = 1,
    val companyNameMalayalam: String = "എസ്.കെ. സ്റ്റോർസ് & സൂപ്പർമാർക്കറ്റ്",
    val companyNameEnglish: String = "SK Stores & Supermarket",
    val tagline: String = "മൊത്ത-ചില്ലറ വ്യാപാരി (Wholesale & Retail Merchant)",
    val address: String = "എം.ജി. റോഡ്, എറണാകുളം, കേരളം - 682011",
    val phone: String = "+91 98470 12345",
    val gstin: String = "32ABCDE1234F1Z5",
    val upiId: String = "9847012345@upi",
    val logoPath: String? = null
)

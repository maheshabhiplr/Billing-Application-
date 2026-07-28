package com.example.data

import androidx.room.*
import com.example.model.CompanyDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM company_details WHERE id = 1")
    fun getCompanyDetails(): Flow<CompanyDetails?>

    @Query("SELECT * FROM company_details WHERE id = 1")
    suspend fun getCompanyDetailsOnce(): CompanyDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCompanyDetails(companyDetails: CompanyDetails)
}

package com.example.data

import androidx.room.*
import com.example.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY nameMalayalam ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE nameMalayalam LIKE '%' || :query || '%' OR nameEnglish LIKE '%' || :query || '%' ORDER BY nameMalayalam ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET pricePerUnit = :newPrice, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updateProductPrice(productId: Long, newPrice: Double, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProduct(product: Product)
}

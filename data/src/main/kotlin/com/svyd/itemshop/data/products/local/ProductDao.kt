package com.svyd.itemshop.data.products.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ProductDao {

    // Ordered by `createdAt` so the grid reflects the order Instagram
    // returns posts in (chronological newest-first) and stays stable across
    // syncs — `updatedAt` would shift on every reconciliation because the
    // sync re-reads title/price/cover from the caption on every pass.
    @Query("SELECT * FROM products ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)
}

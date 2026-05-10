package com.svyd.itemshop.domain.products

import com.svyd.itemshop.domain.common.DomainResult
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    /** Hot stream of all products, sorted newest-first. */
    fun observeAll(): Flow<List<Product>>

    suspend fun getById(id: ProductId): Product?

    /** Insert or update. Returns the persisted [Product]. */
    suspend fun save(product: Product): DomainResult<Product>

    suspend fun delete(id: ProductId): DomainResult<Unit>
}

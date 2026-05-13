package com.svyd.itemshop.data.products

import com.svyd.itemshop.data.common.runCatchingDomain
import com.svyd.itemshop.data.products.local.ProductDao
import com.svyd.itemshop.data.products.mapper.toDomain
import com.svyd.itemshop.data.products.mapper.toEntity
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomProductsRepository(
    private val dao: ProductDao,
) : ProductsRepository {

    override fun observeAll(): Flow<List<Product>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: ProductId): Product? =
        dao.getById(id.raw)?.toDomain()

    override suspend fun save(product: Product): DomainResult<Product> = runCatchingDomain {
        dao.upsert(product.toEntity())
        product
    }

    override suspend fun delete(id: ProductId): DomainResult<Unit> = runCatchingDomain {
        dao.deleteById(id.raw)
    }
}

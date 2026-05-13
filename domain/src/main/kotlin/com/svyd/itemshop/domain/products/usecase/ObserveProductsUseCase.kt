package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductsRepository
import kotlinx.coroutines.flow.Flow

class ObserveProductsUseCase(
    private val repository: ProductsRepository,
) {
    operator fun invoke(): Flow<List<Product>> = repository.observeAll()
}

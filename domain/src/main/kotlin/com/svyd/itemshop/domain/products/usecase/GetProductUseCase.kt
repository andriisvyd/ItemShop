package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductsRepository

class GetProductUseCase(
    private val repository: ProductsRepository,
) {
    suspend operator fun invoke(id: ProductId): Product? = repository.getById(id)
}

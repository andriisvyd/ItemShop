package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductsRepository

class DeleteProductUseCase(
    private val repository: ProductsRepository,
) {
    suspend operator fun invoke(id: ProductId): DomainResult<Unit> = repository.delete(id)
}

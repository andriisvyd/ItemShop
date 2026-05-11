package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.common.DomainError
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ProductsRepository
import kotlinx.datetime.Clock

/**
 * Reverts a [ProductStatus.ReadyToShip] product back to
 * [ProductStatus.Available]. Shipping details collected for the previous
 * transition are intentionally **dropped**: if the same product is marked
 * Ready-to-ship again later, the user re-enters the buyer's info. This
 * keeps the domain simple and avoids holding stale data on a product
 * that's been put back on sale.
 */
class RevertToAvailableUseCase(
    private val repository: ProductsRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: ProductId): DomainResult<Product> {
        val current = repository.getById(id)
            ?: return DomainResult.Failure(DomainError.NotFound("product:${id.raw}"))

        if (current.status !is ProductStatus.ReadyToShip) {
            return DomainResult.Failure(
                DomainError.Validation(
                    field = "status",
                    reason = "Only Ready-to-ship products can be reverted to Available",
                ),
            )
        }

        return repository.save(
            current.copy(
                status = ProductStatus.Available,
                updatedAt = clock.now(),
            ),
        )
    }
}

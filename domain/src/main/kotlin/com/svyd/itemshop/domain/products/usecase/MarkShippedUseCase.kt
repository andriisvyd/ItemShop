package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.common.DomainError
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ProductsRepository
import kotlinx.datetime.Clock

/**
 * Transitions a product from [ProductStatus.ReadyToShip] to
 * [ProductStatus.Shipped], carrying the previously-collected shipping
 * details forward unchanged. Fails with [DomainError.Validation] if the
 * current status is anything other than ReadyToShip.
 */
class MarkShippedUseCase(
    private val repository: ProductsRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: ProductId): DomainResult<Product> {
        val current = repository.getById(id)
            ?: return DomainResult.Failure(DomainError.NotFound("product:${id.raw}"))

        val readyToShip = current.status as? ProductStatus.ReadyToShip
            ?: return DomainResult.Failure(
                DomainError.Validation(
                    field = "status",
                    reason = "Only Ready-to-ship products can be marked Shipped",
                ),
            )

        return repository.save(
            current.copy(
                status = ProductStatus.Shipped(readyToShip.shipping),
                updatedAt = clock.now(),
            ),
        )
    }
}

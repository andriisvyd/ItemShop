package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.common.DomainError
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ProductsRepository
import com.svyd.itemshop.domain.products.ShippingDetails
import kotlinx.datetime.Clock

/**
 * Transitions a product from [ProductStatus.Available] to
 * [ProductStatus.ReadyToShip], attaching the buyer's shipping details.
 * Fails with [DomainError.Validation] if the current status is anything
 * other than Available.
 */
class MarkReadyToShipUseCase(
    private val repository: ProductsRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        id: ProductId,
        shipping: ShippingDetails,
    ): DomainResult<Product> {
        val current = repository.getById(id)
            ?: return DomainResult.Failure(DomainError.NotFound("product:${id.raw}"))

        if (current.status !is ProductStatus.Available) {
            return DomainResult.Failure(
                DomainError.Validation(
                    field = "status",
                    reason = "Only Available products can be marked Ready to ship",
                ),
            )
        }

        return repository.save(
            current.copy(
                status = ProductStatus.ReadyToShip(shipping),
                updatedAt = clock.now(),
            ),
        )
    }
}

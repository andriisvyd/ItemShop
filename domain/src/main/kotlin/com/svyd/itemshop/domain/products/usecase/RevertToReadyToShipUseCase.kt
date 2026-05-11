package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.common.DomainError
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ProductsRepository
import kotlinx.datetime.Clock

/**
 * Reverts a [ProductStatus.Shipped] product back to
 * [ProductStatus.ReadyToShip], preserving the shipping details that were
 * already collected. The presentation layer is expected to prompt for
 * confirmation before invoking this — at the domain level the revert is
 * unconditional.
 */
class RevertToReadyToShipUseCase(
    private val repository: ProductsRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: ProductId): DomainResult<Product> {
        val current = repository.getById(id)
            ?: return DomainResult.Failure(DomainError.NotFound("product:${id.raw}"))

        val shipped = current.status as? ProductStatus.Shipped
            ?: return DomainResult.Failure(
                DomainError.Validation(
                    field = "status",
                    reason = "Only Shipped products can be reverted to Ready to ship",
                ),
            )

        return repository.save(
            current.copy(
                status = ProductStatus.ReadyToShip(shipped.shipping),
                updatedAt = clock.now(),
            ),
        )
    }
}

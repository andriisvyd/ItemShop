package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductsRepository

/**
 * Unconditionally writes a product snapshot back to storage. Used by the
 * undo path on the home screen: every status transition captures the
 * prior product and stashes it in the snackbar event; tapping "Undo"
 * replays that snapshot here, bypassing the transition use cases'
 * status-guard checks (the snapshot is, by construction, a state we just
 * came from — so it's known-valid).
 *
 * This is the only path that writes a product directly without going
 * through a transition. Keep it that way.
 */
class RestoreProductUseCase(
    private val repository: ProductsRepository,
) {
    suspend operator fun invoke(product: Product): DomainResult<Product> =
        repository.save(product)
}

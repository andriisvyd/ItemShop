package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.common.DomainError
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductDraft
import com.svyd.itemshop.domain.products.ProductsRepository
import kotlinx.datetime.Clock

/**
 * Validates a [ProductDraft] and upserts it. The draft id is always set
 * (the Instagram media id), so there's no create/edit branching here:
 * the use case looks up any existing record to preserve `createdAt` and
 * always stamps `updatedAt = now`.
 */
class SaveProductUseCase(
    private val repository: ProductsRepository,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(draft: ProductDraft): DomainResult<Product> {
        val title = draft.title.trim()
        if (title.isEmpty()) {
            return DomainResult.Failure(
                DomainError.Validation(field = "title", reason = "Title must not be empty"),
            )
        }

        val now = clock.now()
        val existing = repository.getById(draft.id)
        val product = Product(
            id = draft.id,
            title = title,
            description = draft.description,
            price = draft.price,
            coverImageUrl = draft.coverImageUrl,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        return repository.save(product)
    }
}

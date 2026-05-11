package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.posts.InstagramPost
import com.svyd.itemshop.domain.posts.InstagramPostsRepository
import com.svyd.itemshop.domain.products.Price
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ProductsRepository
import kotlinx.datetime.Clock

/**
 * One-shot reconciliation between Instagram media and local products.
 *
 * For every post returned by [InstagramPostsRepository]:
 *   - If a product with that id does not exist locally, create it with
 *     title/price derived from the caption (falling back to
 *     [fallbackTitle] when the caption produces nothing usable) and
 *     status [ProductStatus.Available].
 *   - If a product already exists, re-read title / price / cover image
 *     from the latest caption — Instagram is the source of truth for
 *     those fields — but never touch status or shipping info that the
 *     user has accumulated.
 *
 * The fallback title is supplied at construction time so the domain
 * doesn't have to know about Android string resources.
 */
class SyncProductsFromInstagramUseCase(
    private val instagramPostsRepository: InstagramPostsRepository,
    private val productsRepository: ProductsRepository,
    private val clock: Clock,
    private val fallbackTitle: String,
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        return when (val fetched = instagramPostsRepository.loadPosts()) {
            is DomainResult.Failure -> fetched
            is DomainResult.Success -> {
                fetched.value.forEach { post -> reconcile(post) }
                DomainResult.Success(Unit)
            }
        }
    }

    private suspend fun reconcile(post: InstagramPost) {
        val productId = ProductId(post.id)
        val title = extractTitle(post.caption).ifBlank { fallbackTitle }
        val price = Price.parseFromCaption(post.caption)
        val existing = productsRepository.getById(productId)
        val now = clock.now()

        val product = if (existing == null) {
            Product(
                id = productId,
                title = title,
                price = price,
                coverImageUrl = post.imageUrl,
                status = ProductStatus.Available,
                createdAt = now,
                updatedAt = now,
            )
        } else {
            existing.copy(
                title = title,
                price = price,
                coverImageUrl = post.imageUrl,
                updatedAt = now,
            )
        }
        // Best-effort: persistence failures for individual rows are
        // swallowed; the outer use case still reports overall success
        // because the upstream fetch succeeded.
        productsRepository.save(product)
    }

    /**
     * First non-status line of the caption, trimmed. If the first line is
     * a known status marker ("Бронька" / "Забрали", case-insensitive), the
     * second line is used. Empty for blank captions.
     */
    private fun extractTitle(caption: String?): String {
        if (caption.isNullOrBlank()) return ""
        val lines = caption.lines()
        val first = lines.firstOrNull()?.trim().orEmpty()
        return if (first.isStatusMarker()) {
            lines.getOrNull(1)?.trim().orEmpty()
        } else {
            first
        }
    }

    private fun String.isStatusMarker(): Boolean =
        trim().lowercase() in STATUS_MARKERS

    private companion object {
        // Interim heuristic; will be replaced with explicit emoji markers
        // alongside the upcoming Booked status.
        val STATUS_MARKERS = setOf("бронька", "забрали")
    }
}

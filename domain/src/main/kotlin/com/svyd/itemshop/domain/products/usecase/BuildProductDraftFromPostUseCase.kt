package com.svyd.itemshop.domain.products.usecase

import com.svyd.itemshop.domain.posts.InstagramPost
import com.svyd.itemshop.domain.products.Price
import com.svyd.itemshop.domain.products.ProductDraft
import com.svyd.itemshop.domain.products.ProductId

/**
 * Pure mapper from an Instagram post to a fresh product draft. The draft
 * shares its identity with the source post (`id == post.id`), so every
 * product is a 1:1 extension of one media item.
 *
 * Caption rules:
 *  - The first line becomes the title — unless it's a known status
 *    marker ("Бронька" / "Забрали" / case-insensitive match), in which
 *    case the second line is taken instead.
 *  - Price is parsed via the `🏷️<amount><currency>` token anywhere in
 *    the caption.
 *
 * Status markers will become explicit emoji prefixes in a follow-up; the
 * literal-word matching here is interim.
 */
class BuildProductDraftFromPostUseCase {
    operator fun invoke(post: InstagramPost): ProductDraft = ProductDraft(
        id = ProductId(post.id),
        title = extractTitle(post.caption),
        price = Price.parseFromCaption(post.caption),
        coverImageUrl = post.imageUrl,
    )

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

    companion object {
        // Case-insensitive matches. Expand when emoji status markers land.
        private val STATUS_MARKERS = setOf("бронька", "забрали")
    }
}

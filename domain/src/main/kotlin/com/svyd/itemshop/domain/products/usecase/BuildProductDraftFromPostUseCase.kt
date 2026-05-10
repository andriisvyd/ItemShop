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
 *    case the second line is taken as the title and both the status line
 *    and the title line are dropped from the description.
 *  - Description = the caption lines that remain after stripping the
 *    title line (and the status line, when present).
 *  - Price is parsed via the `🏷️<amount><currency>` token anywhere in
 *    the caption.
 *
 * Status markers will become explicit emoji prefixes in a follow-up; the
 * literal-word matching here is interim.
 */
class BuildProductDraftFromPostUseCase {
    operator fun invoke(post: InstagramPost): ProductDraft {
        val (title, description) = extractTitleAndDescription(post.caption)
        return ProductDraft(
            id = ProductId(post.id),
            title = title,
            description = description,
            price = Price.parseFromCaption(post.caption),
            coverImageUrl = post.thumbnailUrl ?: post.mediaUrl,
        )
    }

    private fun extractTitleAndDescription(caption: String?): Pair<String, String> {
        if (caption.isNullOrBlank()) return "" to ""
        val lines = caption.lines()
        val first = lines.firstOrNull()?.trim().orEmpty()
        return if (first.isStatusMarker()) {
            val title = lines.getOrNull(1)?.trim().orEmpty()
            val description = lines.drop(2).joinToString(separator = "\n").trim()
            title to description
        } else {
            val description = lines.drop(1).joinToString(separator = "\n").trim()
            first to description
        }
    }

    private fun String.isStatusMarker(): Boolean =
        trim().lowercase() in STATUS_MARKERS

    companion object {
        // Case-insensitive matches. Expand when emoji status markers land.
        private val STATUS_MARKERS = setOf("бронька", "забрали")
    }
}

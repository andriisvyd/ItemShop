package com.svyd.itemshop.data.posts.mapper

import com.svyd.itemshop.data.posts.remote.dto.MediaDto
import com.svyd.itemshop.domain.posts.InstagramPost
import kotlinx.datetime.Instant

/**
 * Returns null for media items without a usable image URL or timestamp.
 * Callers (the repository) filter these out rather than surfacing
 * partially-broken posts to the UI.
 *
 * Image resolution: prefer `thumbnail_url` (always an image, even for
 * video posts); fall back to `media_url` (the post's own media for image
 * posts). Carousel albums have neither at the top level and so are
 * dropped here — full carousel support is a follow-up.
 */
internal fun MediaDto.toDomainOrNull(): InstagramPost? {
    val imageUrl = thumbnailUrl ?: mediaUrl ?: return null
    val timestamp = timestamp?.let(::parseInstantOrNull) ?: return null
    return InstagramPost(
        id = id,
        caption = caption,
        imageUrl = imageUrl,
        timestamp = timestamp,
    )
}

/**
 * Instagram Graph returns ISO 8601-like timestamps whose timezone offset
 * often omits the colon (`...+0000`). `kotlinx.datetime.Instant.parse`
 * expects RFC 3339 (`...+00:00`). Normalize `±HHMM` at end of string
 * before parsing.
 */
private fun parseInstantOrNull(raw: String): Instant? {
    val trimmed = raw.trim()
    val normalized = trimmed.replace(Regex("""([+-])(\d{2})(\d{2})$""")) { m ->
        "${m.groupValues[1]}${m.groupValues[2]}:${m.groupValues[3]}"
    }
    return runCatching { Instant.parse(normalized) }.getOrNull()
}

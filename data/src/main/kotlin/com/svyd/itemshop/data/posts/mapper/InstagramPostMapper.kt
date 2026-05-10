package com.svyd.itemshop.data.posts.mapper

import com.svyd.itemshop.data.posts.remote.dto.MediaDto
import com.svyd.itemshop.domain.posts.InstagramMediaType
import com.svyd.itemshop.domain.posts.InstagramPost
import kotlinx.datetime.Instant

/**
 * Returns null for media items that don't have a usable image URL or
 * timestamp. Callers (the repository) filter these out rather than
 * surfacing partially-broken posts to the UI.
 */
internal fun MediaDto.toDomainOrNull(): InstagramPost? {
    val mediaUrl = mediaUrl ?: return null
    val timestamp = timestamp?.let(::parseInstantOrNull) ?: return null
    return InstagramPost(
        id = id,
        caption = caption,
        mediaType = mapMediaType(mediaType),
        mediaUrl = mediaUrl,
        thumbnailUrl = thumbnailUrl,
        permalink = permalink ?: "",
        timestamp = timestamp,
    )
}

private fun mapMediaType(raw: String?): InstagramMediaType = when (raw) {
    "IMAGE" -> InstagramMediaType.Image
    "VIDEO" -> InstagramMediaType.Video
    "CAROUSEL_ALBUM" -> InstagramMediaType.CarouselAlbum
    else -> InstagramMediaType.Unknown
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

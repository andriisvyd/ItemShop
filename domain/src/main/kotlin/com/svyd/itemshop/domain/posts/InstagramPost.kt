package com.svyd.itemshop.domain.posts

import kotlinx.datetime.Instant

/**
 * A single Instagram media item belonging to the authenticated user.
 * `mediaUrl` is the cover/main image; for carousels we only store the
 * first child for now (full carousel support is a follow-up).
 */
data class InstagramPost(
    val id: String,
    val caption: String?,
    val mediaType: InstagramMediaType,
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val permalink: String,
    val timestamp: Instant,
)

enum class InstagramMediaType {
    Image,
    Video,
    CarouselAlbum,
    Unknown,
}

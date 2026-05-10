package com.svyd.itemshop.domain.posts

import kotlinx.datetime.Instant

/**
 * A single Instagram media item belonging to the authenticated user.
 * `imageUrl` is the rendered preview — for image posts it's the post's own
 * media, for video posts it's the still-frame thumbnail. Resolved in the
 * data-layer mapper so callers don't need to branch on media type.
 */
data class InstagramPost(
    val id: String,
    val caption: String?,
    val imageUrl: String,
    val timestamp: Instant,
)

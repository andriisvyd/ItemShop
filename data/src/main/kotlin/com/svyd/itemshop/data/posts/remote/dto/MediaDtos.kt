package com.svyd.itemshop.data.posts.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MediaListResponseDto(
    @SerialName("data") val data: List<MediaDto> = emptyList(),
    @SerialName("paging") val paging: PagingDto? = null,
)

@Serializable
internal data class MediaDto(
    @SerialName("id") val id: String,
    @SerialName("caption") val caption: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("permalink") val permalink: String? = null,
    @SerialName("timestamp") val timestamp: String? = null,
)

@Serializable
internal data class PagingDto(
    @SerialName("cursors") val cursors: PagingCursorsDto? = null,
    @SerialName("next") val next: String? = null,
)

@Serializable
internal data class PagingCursorsDto(
    @SerialName("before") val before: String? = null,
    @SerialName("after") val after: String? = null,
)

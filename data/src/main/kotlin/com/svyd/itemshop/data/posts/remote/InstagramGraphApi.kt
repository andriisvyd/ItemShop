package com.svyd.itemshop.data.posts.remote

import com.svyd.itemshop.data.auth.InstagramOAuthConfig
import com.svyd.itemshop.data.posts.remote.dto.MediaDto
import com.svyd.itemshop.data.posts.remote.dto.MediaListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Wraps the few Instagram Graph endpoints we need. The access token is
 * passed in by callers (the repository looks it up from `TokenStorage`),
 * so this class is stateless and easy to test. DTOs never escape.
 */
internal class InstagramGraphApi(
    private val httpClient: HttpClient,
    private val config: InstagramOAuthConfig,
) {

    suspend fun fetchMyMedia(accessToken: String, limit: Int = DEFAULT_LIMIT): MediaListResponseDto =
        httpClient.get("${config.graphBaseUrl}/me/media") {
            parameter("fields", DEFAULT_MEDIA_FIELDS)
            parameter("limit", limit)
            parameter("access_token", accessToken)
        }.body()

    suspend fun fetchMedia(id: String, accessToken: String): MediaDto =
        httpClient.get("${config.graphBaseUrl}/$id") {
            parameter("fields", DEFAULT_MEDIA_FIELDS)
            parameter("access_token", accessToken)
        }.body()

    companion object {
        private const val DEFAULT_LIMIT = 25
        private const val DEFAULT_MEDIA_FIELDS =
            "id,caption,media_url,thumbnail_url,timestamp"
    }
}

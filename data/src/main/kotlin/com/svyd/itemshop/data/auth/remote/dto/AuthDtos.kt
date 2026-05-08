package com.svyd.itemshop.data.auth.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Short-lived token response, returned by the `/oauth/access_token`
 * exchange. We don't persist this DTO directly; it is mapped to a domain
 * `AuthSession` by the repository.
 */
@Serializable
internal data class AccessTokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("user_id") val userId: Long,
    @SerialName("permissions") val permissions: String? = null,
)

/**
 * Long-lived token (60 days) returned by the `/access_token` upgrade
 * endpoint. Same shape regardless of whether it's an initial exchange or
 * a refresh.
 */
@Serializable
internal data class LongLivedTokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresInSeconds: Long? = null,
)

@Serializable
internal data class GraphUserDto(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("account_type") val accountType: String? = null,
)

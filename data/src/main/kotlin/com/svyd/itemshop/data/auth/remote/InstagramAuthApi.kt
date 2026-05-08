package com.svyd.itemshop.data.auth.remote

import com.svyd.itemshop.data.auth.InstagramOAuthConfig
import com.svyd.itemshop.data.auth.remote.dto.AccessTokenResponseDto
import com.svyd.itemshop.data.auth.remote.dto.GraphUserDto
import com.svyd.itemshop.data.auth.remote.dto.LongLivedTokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Parameters

/**
 * Thin wrapper over the Ktor client that exposes the few Instagram OAuth
 * endpoints the auth repository needs. No domain types here — this layer
 * deals strictly in DTOs.
 */
internal class InstagramAuthApi(
    private val httpClient: HttpClient,
    private val config: InstagramOAuthConfig,
) {

    suspend fun exchangeCodeForToken(authorizationCode: String): AccessTokenResponseDto {
        return httpClient.submitForm(
            url = config.tokenEndpoint,
            formParameters = Parameters.build {
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("grant_type", "authorization_code")
                append("redirect_uri", config.redirectUri)
                append("code", authorizationCode)
            },
        ).body()
    }

    suspend fun upgradeToLongLivedToken(shortLivedToken: String): LongLivedTokenResponseDto {
        return httpClient.get("${config.graphBaseUrl}/access_token") {
            parameter("grant_type", "ig_exchange_token")
            parameter("client_secret", config.clientSecret)
            parameter("access_token", shortLivedToken)
        }.body()
    }

    suspend fun fetchSelf(accessToken: String): GraphUserDto {
        return httpClient.get("${config.graphBaseUrl}/me") {
            parameter("fields", "id,username,account_type")
            parameter("access_token", accessToken)
        }.body()
    }
}

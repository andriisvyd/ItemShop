package com.svyd.itemshop.data.auth

/**
 * All Instagram-specific OAuth knobs in one place. Constructed by the DI
 * container from BuildConfig values that are themselves sourced from
 * `local.properties`, so credentials never live in version control.
 */
data class InstagramOAuthConfig(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: List<String> = DEFAULT_SCOPES,
    val authorizationEndpoint: String = AUTHORIZATION_ENDPOINT,
    val tokenEndpoint: String = TOKEN_ENDPOINT,
    val graphBaseUrl: String = GRAPH_BASE_URL,
) {
    val isConfigured: Boolean
        get() = clientId.isNotBlank() && redirectUri.isNotBlank()

    companion object {
        // Instagram Login API endpoints (the post-Basic-Display surface).
        const val AUTHORIZATION_ENDPOINT = "https://www.instagram.com/oauth/authorize"
        const val TOKEN_ENDPOINT = "https://api.instagram.com/oauth/access_token"
        const val GRAPH_BASE_URL = "https://graph.instagram.com"

        val DEFAULT_SCOPES = listOf(
            "instagram_business_basic",
            "instagram_business_content_publish",
        )
    }
}

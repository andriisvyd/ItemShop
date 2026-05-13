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

        /**
         * App-Links-verified callback URL. Hardcoded because:
         *  - Lint enforces literal `scheme` / `host` for autoVerify intent
         *    filters,
         *  - the URL is bound to the Cloudflare-hosted `assetlinks.json`
         *    fingerprint and to the Redirect URL registered in the Meta
         *    dashboard, so it can't differ across builds without breaking
         *    verification.
         * If you change this, update the manifest `<data>` element and
         * the `assetlinks.json` host in tandem.
         */
        const val REDIRECT_URI = "https://item-shop.pages.dev/oauth/callback"

        val DEFAULT_SCOPES = listOf(
            "instagram_business_basic",
            "instagram_business_content_publish",
        )
    }
}

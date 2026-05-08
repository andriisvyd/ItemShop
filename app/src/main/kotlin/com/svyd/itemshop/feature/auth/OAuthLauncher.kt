package com.svyd.itemshop.feature.auth

/**
 * Platform-agnostic contract for opening an OAuth authorization URL.
 *
 * On Android the implementation uses Chrome Custom Tabs to keep the user's
 * existing browser session and avoid embedding a WebView (discouraged by
 * both Google and Meta). On other platforms (KMP) this would be a SFSafari
 * controller, system browser, etc.
 */
fun interface OAuthLauncher {
    fun launch(url: String)
}

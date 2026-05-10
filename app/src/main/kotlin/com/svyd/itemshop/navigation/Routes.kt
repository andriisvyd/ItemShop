package com.svyd.itemshop.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe destinations used by the app. Compose Navigation 2.8+ supports
 * passing these as `@Serializable` types directly, removing the need for
 * stringly-typed routes and argument keys.
 */
sealed interface Route {

    @Serializable
    data object Login : Route

    @Serializable
    data object Products : Route

    @Serializable
    data object Posts : Route

    /**
     * Edit (or create) a product. The `id` is the originating Instagram
     * media id; the screen looks up the product locally and falls back to
     * fetching the post and building a fresh draft if no local record
     * exists yet.
     */
    @Serializable
    data class EditProduct(val id: String) : Route
}

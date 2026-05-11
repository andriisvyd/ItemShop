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

    /** Single landing page for the signed-in user; replaces Products + Posts. */
    @Serializable
    data object Home : Route

    @Serializable
    data object Products : Route

    @Serializable
    data object Posts : Route

    /**
     * Read-only details for an existing product. `id` is the originating
     * Instagram media id (which is also the product's primary key).
     */
    @Serializable
    data class ProductDetails(val id: String) : Route

    /**
     * Create a product from an Instagram post. `id` is the post id. The
     * screen will refuse to clobber an existing product and prompts the
     * user to open it for viewing instead.
     */
    @Serializable
    data class EditProduct(val id: String) : Route
}

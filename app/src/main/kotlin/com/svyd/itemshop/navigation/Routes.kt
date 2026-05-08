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
     * Create-or-edit screen.
     *
     * @param productId existing product to edit, or null to create.
     * @param sourcePostId Instagram post the new product is derived from
     *                     (used only on creation).
     */
    @Serializable
    data class EditProduct(
        val productId: String? = null,
        val sourcePostId: String? = null,
    ) : Route
}

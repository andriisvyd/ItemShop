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

    /** Single landing page for the signed-in user. */
    @Serializable
    data object Home : Route

    // The following routes are no longer reachable. Kept declared so the
    // legacy `feature/posts/` and `feature/products/{list,edit}/` packages
    // continue to compile until they're deleted in the upcoming cleanup
    // pass.
    @Serializable
    data object Products : Route

    @Serializable
    data object Posts : Route

    @Serializable
    data class EditProduct(val id: String) : Route
}

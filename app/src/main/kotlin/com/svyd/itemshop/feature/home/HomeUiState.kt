package com.svyd.itemshop.feature.home

import com.svyd.itemshop.domain.products.Product

sealed interface HomeUiState {

    /** First-time sync has been kicked off but nothing is in the DB yet. */
    data object InitialSync : HomeUiState

    /** Initial sync failed with no local data to fall back on. */
    data class InitialSyncFailed(val message: String) : HomeUiState

    /**
     * Grid is renderable. `products` may be empty if Instagram returned
     * nothing for the user. `refreshError` is a transient banner for
     * subsequent sync failures (when we have prior data to keep showing).
     */
    data class Content(
        val products: List<Product>,
        val isRefreshing: Boolean = false,
        val refreshError: String? = null,
    ) : HomeUiState
}

package com.svyd.itemshop.feature.posts

import com.svyd.itemshop.domain.posts.InstagramPost

sealed interface PostsListUiState {
    data object Loading : PostsListUiState
    data object Empty : PostsListUiState

    /**
     * Loaded grid of posts. Two transient slots:
     *  - [prompt]: a confirmation dialog asking whether to open an
     *    already-converted product instead of creating a new one.
     *  - [pendingNavigateToEditId]: a one-shot signal that the screen
     *    should navigate to the EditProduct flow for this post id.
     *    Cleared by the screen via `onNavigationConsumed` after
     *    triggering the navigation.
     */
    data class Content(
        val posts: List<InstagramPost>,
        val prompt: ExistingProductPrompt? = null,
        val pendingNavigateToEditId: String? = null,
    ) : PostsListUiState

    data class Error(val message: String) : PostsListUiState
}

data class ExistingProductPrompt(
    val productId: String,
    val productTitle: String,
)

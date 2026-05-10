package com.svyd.itemshop.feature.posts

import com.svyd.itemshop.domain.posts.InstagramPost

sealed interface PostsListUiState {
    data object Loading : PostsListUiState
    data object Empty : PostsListUiState
    data class Content(val posts: List<InstagramPost>) : PostsListUiState
    data class Error(val message: String) : PostsListUiState
}

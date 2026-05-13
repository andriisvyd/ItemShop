package com.svyd.itemshop.feature.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.posts.usecase.LoadInstagramPostsUseCase
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.usecase.GetProductUseCase
import com.svyd.itemshop.ui.error.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the posts grid. Beyond loading the user's media, it also
 * detects when a tapped post is already a saved product and surfaces a
 * prompt — so the flow that picks a post for product creation never
 * silently overwrites existing work.
 */
class PostsListViewModel(
    private val loadPosts: LoadInstagramPostsUseCase,
    private val getProduct: GetProductUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<PostsListUiState>(PostsListUiState.Loading)
    val state: StateFlow<PostsListUiState> = _state.asStateFlow()

    private var loadingJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (loadingJob?.isActive == true) return
        _state.value = PostsListUiState.Loading
        loadingJob = viewModelScope.launch {
            _state.value = when (val result = loadPosts()) {
                is DomainResult.Success ->
                    if (result.value.isEmpty()) PostsListUiState.Empty
                    else PostsListUiState.Content(posts = result.value)
                is DomainResult.Failure -> PostsListUiState.Error(
                    message = result.error.toUserMessage(),
                )
            }
        }
    }

    /**
     * Resolve a post tap: if a product with that id already exists, show
     * the prompt; otherwise emit a navigate-to-edit signal for the screen
     * to consume.
     */
    fun onPostClicked(postId: String) {
        val current = _state.value as? PostsListUiState.Content ?: return
        if (current.prompt != null || current.pendingNavigateToEditId != null) return

        viewModelScope.launch {
            val existing = getProduct(ProductId(postId))
            _state.update { latest ->
                (latest as? PostsListUiState.Content)?.copy(
                    prompt = existing?.let {
                        ExistingProductPrompt(productId = it.id.raw, productTitle = it.title)
                    },
                    pendingNavigateToEditId = if (existing == null) postId else null,
                ) ?: latest
            }
        }
    }

    fun onPromptDismissed() {
        _state.update { latest ->
            (latest as? PostsListUiState.Content)?.copy(prompt = null) ?: latest
        }
    }

    fun onNavigationConsumed() {
        _state.update { latest ->
            (latest as? PostsListUiState.Content)?.copy(pendingNavigateToEditId = null) ?: latest
        }
    }
}

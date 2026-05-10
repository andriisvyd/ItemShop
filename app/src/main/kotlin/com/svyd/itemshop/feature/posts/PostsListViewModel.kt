package com.svyd.itemshop.feature.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.posts.usecase.LoadInstagramPostsUseCase
import com.svyd.itemshop.ui.error.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostsListViewModel(
    private val loadPosts: LoadInstagramPostsUseCase,
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
                    else PostsListUiState.Content(result.value)
                is DomainResult.Failure -> PostsListUiState.Error(
                    message = result.error.toUserMessage(),
                )
            }
        }
    }
}

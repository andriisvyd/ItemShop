package com.svyd.itemshop.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.usecase.ObserveProductsUseCase
import com.svyd.itemshop.domain.products.usecase.SyncProductsFromInstagramUseCase
import com.svyd.itemshop.ui.error.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the home grid. State is derived from two sources combined:
 *  - the local product list (a hot Flow from Room),
 *  - a `SyncStatus` that tracks the most recent Instagram sync attempt.
 *
 * The combination determines the four screen states:
 *  - `InitialSync` when nothing is in the DB yet and a sync is running,
 *  - `InitialSyncFailed` when the first sync failed and the DB is still empty,
 *  - `Content` whenever there's anything to show (which includes the
 *    "Instagram returned 0 posts" case as an empty list).
 */
class HomeViewModel(
    observeProducts: ObserveProductsUseCase,
    private val syncProducts: SyncProductsFromInstagramUseCase,
) : ViewModel() {

    private val syncStatus = MutableStateFlow(SyncStatus())

    val state: StateFlow<HomeUiState> = combine(
        observeProducts(),
        syncStatus,
    ) { products, sync -> toUiState(products, sync) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = HomeUiState.InitialSync,
        )

    init {
        refresh()
    }

    fun refresh() {
        if (syncStatus.value.isSyncing) return
        syncStatus.update { it.copy(isSyncing = true, error = null) }
        viewModelScope.launch {
            when (val result = syncProducts()) {
                is DomainResult.Success -> syncStatus.update {
                    it.copy(isSyncing = false, hasSucceededOnce = true, error = null)
                }
                is DomainResult.Failure -> syncStatus.update {
                    it.copy(isSyncing = false, error = result.error.toUserMessage())
                }
            }
        }
    }

    private fun toUiState(products: List<Product>, sync: SyncStatus): HomeUiState = when {
        !sync.hasSucceededOnce && sync.isSyncing && products.isEmpty() -> HomeUiState.InitialSync
        !sync.hasSucceededOnce && sync.error != null && products.isEmpty() ->
            HomeUiState.InitialSyncFailed(sync.error)
        else -> HomeUiState.Content(
            products = products,
            isRefreshing = sync.isSyncing,
            // Only surface refresh errors when we have something to show
            // already; otherwise the InitialSyncFailed state above owns the
            // error UX.
            refreshError = if (sync.hasSucceededOnce) sync.error else null,
        )
    }

    private data class SyncStatus(
        val isSyncing: Boolean = false,
        val hasSucceededOnce: Boolean = false,
        val error: String? = null,
    )
}

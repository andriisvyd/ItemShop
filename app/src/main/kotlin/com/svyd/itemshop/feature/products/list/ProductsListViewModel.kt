package com.svyd.itemshop.feature.products.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.products.usecase.ObserveProductsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class ProductsListViewModel(
    observeProducts: ObserveProductsUseCase,
) : ViewModel() {

    val state: StateFlow<ProductsListUiState> = observeProducts()
        .map<List<com.svyd.itemshop.domain.products.Product>, ProductsListUiState> { products ->
            if (products.isEmpty()) ProductsListUiState.Empty
            else ProductsListUiState.Content(products)
        }
        .onStart { emit(ProductsListUiState.Loading) }
        .catch { t -> emit(ProductsListUiState.Error(t.message ?: "Failed to load products")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = ProductsListUiState.Loading,
        )
}

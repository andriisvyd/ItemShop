package com.svyd.itemshop.feature.products.list

import com.svyd.itemshop.domain.products.Product

sealed interface ProductsListUiState {
    data object Loading : ProductsListUiState
    data object Empty : ProductsListUiState
    data class Content(val products: List<Product>) : ProductsListUiState
    data class Error(val message: String) : ProductsListUiState
}

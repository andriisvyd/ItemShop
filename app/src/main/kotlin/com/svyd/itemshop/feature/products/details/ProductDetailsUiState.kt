package com.svyd.itemshop.feature.products.details

import com.svyd.itemshop.domain.products.Product

sealed interface ProductDetailsUiState {
    data object Loading : ProductDetailsUiState
    data class Content(val product: Product) : ProductDetailsUiState
    data object NotFound : ProductDetailsUiState
}

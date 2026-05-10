package com.svyd.itemshop.feature.products.edit

sealed interface EditProductUiState {

    data object Loading : EditProductUiState

    data class LoadFailed(val message: String) : EditProductUiState

    data class Editing(
        val mode: Mode,
        val form: ProductFormState,
        val isSaving: Boolean = false,
        val saveError: String? = null,
        val saved: Boolean = false,
    ) : EditProductUiState

    /** Determined at init time, not from navigation. */
    enum class Mode { Create, Edit }
}

data class ProductFormState(
    val productId: String,
    val title: String = "",
    val description: String = "",
    val priceAmount: String = "",
    val priceCurrency: String = "",
    val coverImageUrl: String? = null,
    val titleError: String? = null,
    val priceError: String? = null,
)

package com.svyd.itemshop.feature.products.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.usecase.GetProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Read-only product viewer. Edit mode is intentionally not exposed in v1;
 * a future revision will add an "Edit" affordance that re-enters the
 * EditProduct screen pointed at this id.
 */
class ProductDetailsViewModel(
    private val id: String,
    private val getProduct: GetProductUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val state: StateFlow<ProductDetailsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val product = getProduct(ProductId(id))
            _state.value = product?.let(ProductDetailsUiState::Content)
                ?: ProductDetailsUiState.NotFound
        }
    }
}

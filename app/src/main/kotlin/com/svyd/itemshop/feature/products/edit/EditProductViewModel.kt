package com.svyd.itemshop.feature.products.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.posts.usecase.GetInstagramPostUseCase
import com.svyd.itemshop.domain.products.CurrencySymbol
import com.svyd.itemshop.domain.products.Price
import com.svyd.itemshop.domain.products.ProductDraft
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.usecase.BuildProductDraftFromPostUseCase
import com.svyd.itemshop.domain.products.usecase.SaveProductUseCase
import com.svyd.itemshop.ui.error.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the create-product screen. The Instagram media id is received via
 * [id]; the screen fetches the post, builds a draft, and exposes it for
 * editing. It is the caller's responsibility (PostsListViewModel) to
 * ensure the post isn't already a saved product before navigating here.
 *
 * Editing existing products is intentionally not supported in v1.
 */
class EditProductViewModel(
    private val id: String,
    private val getInstagramPost: GetInstagramPostUseCase,
    private val buildDraftFromPost: BuildProductDraftFromPostUseCase,
    private val saveProduct: SaveProductUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<EditProductUiState>(EditProductUiState.Loading)
    val state: StateFlow<EditProductUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { initialise() }
    }

    private suspend fun initialise() {
        when (val result = getInstagramPost(id)) {
            is DomainResult.Success -> {
                val post = result.value
                if (post == null) {
                    _state.value = EditProductUiState.LoadFailed("Instagram post not found")
                    return
                }
                val draft = buildDraftFromPost(post)
                _state.value = EditProductUiState.Editing(
                    form = ProductFormState(
                        productId = draft.id.raw,
                        title = draft.title,
                        priceAmount = draft.price?.let(::formatAmount).orEmpty(),
                        priceCurrency = draft.price?.currency?.raw.orEmpty(),
                        coverImageUrl = draft.coverImageUrl,
                    ),
                )
            }
            is DomainResult.Failure ->
                _state.value = EditProductUiState.LoadFailed(result.error.toUserMessage())
        }
    }

    fun onTitleChange(value: String) = updateForm { it.copy(title = value, titleError = null) }

    fun onPriceAmountChange(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' || it == ',' }
        updateForm { it.copy(priceAmount = sanitized, priceError = null) }
    }

    fun onPriceCurrencyChange(value: String) =
        updateForm { it.copy(priceCurrency = value.trim(), priceError = null) }

    fun onSaveClick() {
        val current = _state.value as? EditProductUiState.Editing ?: return
        if (current.isSaving) return

        val validationError = validate(current.form)
        if (validationError != null) {
            _state.value = current.copy(form = validationError)
            return
        }

        val price = parsePrice(current.form.priceAmount, current.form.priceCurrency)
        val draft = ProductDraft(
            id = ProductId(current.form.productId),
            title = current.form.title,
            price = price,
            coverImageUrl = current.form.coverImageUrl,
        )

        _state.value = current.copy(isSaving = true, saveError = null)
        viewModelScope.launch {
            when (val result = saveProduct(draft)) {
                is DomainResult.Success ->
                    _state.update {
                        (it as? EditProductUiState.Editing)?.copy(isSaving = false, saved = true)
                            ?: it
                    }
                is DomainResult.Failure ->
                    _state.update {
                        (it as? EditProductUiState.Editing)?.copy(
                            isSaving = false,
                            saveError = result.error.toUserMessage(),
                        ) ?: it
                    }
            }
        }
    }

    private fun updateForm(transform: (ProductFormState) -> ProductFormState) {
        _state.update { current ->
            (current as? EditProductUiState.Editing)
                ?.copy(form = transform(current.form))
                ?: current
        }
    }

    /**
     * Returns a [ProductFormState] with field errors filled in if invalid;
     * returns null if everything checks out.
     */
    private fun validate(form: ProductFormState): ProductFormState? {
        val titleError = if (form.title.isBlank()) "Title must not be empty" else null
        val priceError = run {
            val amount = form.priceAmount.trim()
            val currency = form.priceCurrency.trim()
            when {
                amount.isEmpty() && currency.isEmpty() -> null
                amount.isEmpty() -> "Amount is required when currency is set"
                currency.isEmpty() -> "Currency is required when amount is set"
                amount.toAmountMinorOrNull() == null -> "Invalid amount"
                else -> null
            }
        }
        return if (titleError == null && priceError == null) null
        else form.copy(titleError = titleError, priceError = priceError)
    }

    private fun parsePrice(amountText: String, currencyText: String): Price? {
        val trimmedAmount = amountText.trim()
        val trimmedCurrency = currencyText.trim()
        if (trimmedAmount.isEmpty() || trimmedCurrency.isEmpty()) return null
        val amountMinor = trimmedAmount.toAmountMinorOrNull() ?: return null
        return Price(amountMinor = amountMinor, currency = CurrencySymbol(trimmedCurrency))
    }

    private fun formatAmount(price: Price): String {
        val whole = price.amountMinor / 100
        val minor = (price.amountMinor % 100).toInt()
        return if (minor == 0) whole.toString()
        else "$whole.${minor.toString().padStart(2, '0')}"
    }

    private fun String.toAmountMinorOrNull(): Long? {
        val normalized = replace(',', '.')
        val parts = normalized.split('.')
        return when (parts.size) {
            1 -> runCatching { parts[0].toLong() * 100 }.getOrNull()
            2 -> {
                val whole = parts[0].toLongOrNull() ?: return null
                val fractionRaw = parts[1].padEnd(2, '0').take(2)
                val fraction = fractionRaw.toIntOrNull() ?: return null
                whole * 100 + fraction
            }
            else -> null
        }
    }
}

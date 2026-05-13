package com.svyd.itemshop.feature.products.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.PickupPoint
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ShippingDetails
import com.svyd.itemshop.domain.products.usecase.GetProductUseCase
import com.svyd.itemshop.domain.products.usecase.MarkReadyToShipUseCase
import com.svyd.itemshop.domain.products.usecase.MarkShippedUseCase
import com.svyd.itemshop.domain.products.usecase.RevertToAvailableUseCase
import com.svyd.itemshop.domain.products.usecase.RevertToReadyToShipUseCase
import com.svyd.itemshop.ui.error.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Read-and-transition view model for the details screen. Loads the
 * product once at init; subsequent state updates come from the
 * transition use cases, which return the updated `Product` directly.
 *
 * In-flight prompts (the shipping sheet, revert confirmations) are
 * modelled as a `pendingTransition` slot on the Content state, mirroring
 * the `prompt` pattern used by `PostsListViewModel`.
 */
class ProductDetailsViewModel(
    private val id: String,
    private val getProduct: GetProductUseCase,
    private val markReadyToShip: MarkReadyToShipUseCase,
    private val markShipped: MarkShippedUseCase,
    private val revertToReadyToShip: RevertToReadyToShipUseCase,
    private val revertToAvailable: RevertToAvailableUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val state: StateFlow<ProductDetailsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val product = getProduct(ProductId(id))
            _state.value = product?.let { ProductDetailsUiState.Content(product = it) }
                ?: ProductDetailsUiState.NotFound
        }
    }

    // ----- Primary action dispatch -----

    /**
     * Invoked by the screen's primary action button. Branches on the
     * current status: Available opens the shipping sheet, ReadyToShip
     * triggers the direct flip to Shipped. Shipped has no primary action.
     */
    fun onPrimaryActionClick() {
        val content = currentContent() ?: return
        when (content.product.status) {
            ProductStatus.Available -> _state.update {
                content.copy(
                    pendingTransition = PendingTransition.CollectingShipping(
                        form = ShippingFormState(),
                    ),
                    transitionError = null,
                )
            }
            is ProductStatus.ReadyToShip -> runTransition {
                markShipped(ProductId(content.product.id.raw))
            }
            is ProductStatus.Shipped -> Unit
        }
    }

    // ----- Revert prompts -----

    fun onRevertToAvailableClick() {
        _state.update {
            (it as? ProductDetailsUiState.Content)?.copy(
                pendingTransition = PendingTransition.ConfirmRevertToAvailable,
                transitionError = null,
            ) ?: it
        }
    }

    fun onRevertToReadyToShipClick() {
        _state.update {
            (it as? ProductDetailsUiState.Content)?.copy(
                pendingTransition = PendingTransition.ConfirmRevertToReadyToShip,
                transitionError = null,
            ) ?: it
        }
    }

    fun onPendingTransitionDismissed() {
        _state.update {
            (it as? ProductDetailsUiState.Content)?.copy(pendingTransition = null) ?: it
        }
    }

    fun onRevertConfirmed() {
        val content = currentContent() ?: return
        when (content.pendingTransition) {
            PendingTransition.ConfirmRevertToAvailable -> runTransition {
                revertToAvailable(ProductId(content.product.id.raw))
            }
            PendingTransition.ConfirmRevertToReadyToShip -> runTransition {
                revertToReadyToShip(ProductId(content.product.id.raw))
            }
            else -> Unit
        }
    }

    // ----- Shipping form -----

    fun onShippingFullNameChange(value: String) = updateForm {
        it.copy(fullName = value, fullNameError = null)
    }

    fun onShippingPhoneChange(value: String) = updateForm {
        it.copy(phone = value, phoneError = null)
    }

    fun onShippingCityChange(value: String) = updateForm {
        it.copy(city = value, cityError = null)
    }

    fun onShippingPickupTypeChange(value: PickupTypeOption) = updateForm {
        it.copy(pickupType = value)
    }

    fun onShippingPickupNumberChange(value: String) = updateForm {
        it.copy(pickupNumber = value, pickupNumberError = null)
    }

    fun onShippingSubmit() {
        val content = currentContent() ?: return
        val sheet = content.pendingTransition as? PendingTransition.CollectingShipping ?: return
        if (sheet.isSubmitting) return

        val validated = validate(sheet.form)
        if (validated != null) {
            _state.update {
                content.copy(
                    pendingTransition = sheet.copy(form = validated),
                )
            }
            return
        }

        val shipping = ShippingDetails(
            fullName = sheet.form.fullName.trim(),
            phone = sheet.form.phone.trim(),
            city = sheet.form.city.trim(),
            pickupPoint = when (sheet.form.pickupType) {
                PickupTypeOption.PostOffice -> PickupPoint.PostOffice(sheet.form.pickupNumber.trim())
                PickupTypeOption.ParcelLocker -> PickupPoint.ParcelLocker(sheet.form.pickupNumber.trim())
            },
        )

        _state.update {
            content.copy(pendingTransition = sheet.copy(isSubmitting = true))
        }

        viewModelScope.launch {
            when (val result = markReadyToShip(ProductId(content.product.id.raw), shipping)) {
                is DomainResult.Success -> applyTransitionResult(result.value)
                is DomainResult.Failure -> _state.update {
                    (it as? ProductDetailsUiState.Content)?.copy(
                        pendingTransition = sheet.copy(isSubmitting = false),
                        transitionError = result.error.toUserMessage(),
                    ) ?: it
                }
            }
        }
    }

    // ----- Internals -----

    private fun currentContent(): ProductDetailsUiState.Content? =
        _state.value as? ProductDetailsUiState.Content

    private fun runTransition(block: suspend () -> DomainResult<Product>) {
        viewModelScope.launch {
            when (val result = block()) {
                is DomainResult.Success -> applyTransitionResult(result.value)
                is DomainResult.Failure -> _state.update {
                    (it as? ProductDetailsUiState.Content)?.copy(
                        pendingTransition = null,
                        transitionError = result.error.toUserMessage(),
                    ) ?: it
                }
            }
        }
    }

    private fun applyTransitionResult(product: Product) {
        _state.update {
            (it as? ProductDetailsUiState.Content)?.copy(
                product = product,
                pendingTransition = null,
                transitionError = null,
            ) ?: ProductDetailsUiState.Content(product = product)
        }
    }

    private fun updateForm(transform: (ShippingFormState) -> ShippingFormState) {
        _state.update { current ->
            val content = current as? ProductDetailsUiState.Content ?: return@update current
            val sheet = content.pendingTransition as? PendingTransition.CollectingShipping
                ?: return@update current
            content.copy(pendingTransition = sheet.copy(form = transform(sheet.form)))
        }
    }

    /**
     * Returns a [ShippingFormState] with field errors filled in if invalid,
     * or null if everything checks out.
     */
    private fun validate(form: ShippingFormState): ShippingFormState? {
        val fullNameError = if (form.fullName.isBlank()) ValidationMarker else null
        val phoneError = if (form.phone.isBlank()) ValidationMarker else null
        val cityError = if (form.city.isBlank()) ValidationMarker else null
        val pickupNumberError = if (form.pickupNumber.isBlank()) ValidationMarker else null
        return if (fullNameError == null && phoneError == null &&
            cityError == null && pickupNumberError == null
        ) null
        else form.copy(
            fullNameError = fullNameError,
            phoneError = phoneError,
            cityError = cityError,
            pickupNumberError = pickupNumberError,
        )
    }

    private companion object {
        /**
         * Sentinel error marker; the screen resolves it to a localised
         * string (`R.string.error_required_field`). Keeping the VM string-free
         * lets us swap the resource without touching state.
         */
        const val ValidationMarker = "REQUIRED"
    }
}

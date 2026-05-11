package com.svyd.itemshop.feature.products.details

import com.svyd.itemshop.domain.products.Product

sealed interface ProductDetailsUiState {

    data object Loading : ProductDetailsUiState

    data class Content(
        val product: Product,
        val pendingTransition: PendingTransition? = null,
        val transitionError: String? = null,
    ) : ProductDetailsUiState

    data object NotFound : ProductDetailsUiState
}

/**
 * UI-level model for an in-flight status transition. The shipping sheet
 * has its own form state because we collect new data; the two revert
 * transitions just need a confirm/cancel decision.
 */
sealed interface PendingTransition {

    data class CollectingShipping(
        val form: ShippingFormState,
        val isSubmitting: Boolean = false,
    ) : PendingTransition

    data object ConfirmRevertToAvailable : PendingTransition
    data object ConfirmRevertToReadyToShip : PendingTransition
}

data class ShippingFormState(
    val fullName: String = "",
    val phone: String = "",
    val city: String = "",
    val pickupType: PickupTypeOption = PickupTypeOption.PostOffice,
    val pickupNumber: String = "",
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val cityError: String? = null,
    val pickupNumberError: String? = null,
)

/**
 * UI-side enum mirror of the domain `PickupPoint` sealed hierarchy. Kept
 * as an enum here because the form state needs a value-type for radio
 * binding; converted to the sealed type at submit time.
 */
enum class PickupTypeOption { PostOffice, ParcelLocker }

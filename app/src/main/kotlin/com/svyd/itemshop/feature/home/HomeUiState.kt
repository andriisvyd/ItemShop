package com.svyd.itemshop.feature.home

import com.svyd.itemshop.domain.products.Product

sealed interface HomeUiState {

    /** First-time sync has been kicked off but nothing is in the DB yet. */
    data object InitialSync : HomeUiState

    /** Initial sync failed with no local data to fall back on. */
    data class InitialSyncFailed(val message: String) : HomeUiState

    /**
     * Grid is renderable. `products` may be empty if Instagram returned
     * nothing. `pendingTransition` drives modal overlays (shipping
     * sheet / info sheet / revert confirmation alerts). `refreshError`
     * is a transient banner for subsequent sync failures.
     */
    data class Content(
        val products: List<Product>,
        val isRefreshing: Boolean = false,
        val refreshError: String? = null,
        val pendingTransition: PendingTransition? = null,
        val transitionError: String? = null,
    ) : HomeUiState
}

/**
 * Modal overlays driven from tile taps. Always references the target
 * product by id so the screen looks it up against the latest list (the
 * sync runs in the background; the underlying product can change while
 * the user is interacting with a sheet).
 *
 * Revert confirmation is intentionally **not** modelled here — it lives
 * as local Compose state inside the info sheet because it's a transient
 * UI exchange that has no meaning if the sheet itself dismisses. The VM
 * only sees the final outcome via the revert action methods.
 */
sealed interface PendingTransition {

    data class CollectingShipping(
        val productId: String,
        val form: ShippingFormState,
        val isSubmitting: Boolean = false,
    ) : PendingTransition

    data class ViewingInfo(val productId: String) : PendingTransition
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
 * UI-side enum mirror of the domain `PickupPoint` sealed hierarchy. The
 * form holds this enum because radio binding wants a comparable value;
 * conversion to the sealed type happens on submit.
 */
enum class PickupTypeOption { PostOffice, ParcelLocker }

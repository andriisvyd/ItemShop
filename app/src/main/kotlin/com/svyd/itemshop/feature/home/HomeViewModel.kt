package com.svyd.itemshop.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.auth.AuthState
import com.svyd.itemshop.domain.auth.usecase.ObserveAuthStateUseCase
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.products.PickupPoint
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ShippingDetails
import com.svyd.itemshop.domain.products.usecase.MarkReadyToShipUseCase
import com.svyd.itemshop.domain.products.usecase.MarkShippedUseCase
import com.svyd.itemshop.domain.products.usecase.ObserveProductsUseCase
import com.svyd.itemshop.domain.products.usecase.RestoreProductUseCase
import com.svyd.itemshop.domain.products.usecase.RevertToAvailableUseCase
import com.svyd.itemshop.domain.products.usecase.RevertToReadyToShipUseCase
import com.svyd.itemshop.domain.products.usecase.SyncProductsFromInstagramUseCase
import com.svyd.itemshop.ui.error.toUserMessage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the home grid plus all status transitions. The screen has no
 * navigation away from itself — tile interactions open modal overlays
 * (shipping sheet, info sheet, revert confirmation dialog) instead.
 *
 * Snackbar notifications about successful transitions go through a
 * one-shot `Channel`: the screen collects events and calls
 * `SnackbarHostState.showSnackbar`; if the user taps "Undo", the
 * captured prior `Product` snapshot is replayed through
 * `RestoreProductUseCase`.
 */
class HomeViewModel(
    observeProducts: ObserveProductsUseCase,
    observeAuthState: ObserveAuthStateUseCase,
    private val syncProducts: SyncProductsFromInstagramUseCase,
    private val markReadyToShip: MarkReadyToShipUseCase,
    private val markShipped: MarkShippedUseCase,
    private val revertToReadyToShip: RevertToReadyToShipUseCase,
    private val revertToAvailable: RevertToAvailableUseCase,
    private val restoreProduct: RestoreProductUseCase,
) : ViewModel() {

    private val productsFlow = observeProducts()
    private val syncStatus = MutableStateFlow(SyncStatus())
    private val transitionState = MutableStateFlow(TransitionState())

    val state: StateFlow<HomeUiState> = combine(
        productsFlow,
        syncStatus,
        transitionState,
    ) { products, sync, transition -> toUiState(products, sync, transition) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = HomeUiState.InitialSync,
        )

    /**
     * Authenticated user's Instagram handle, used as the home screen's
     * top app bar title. Null until the auth state resolves; in practice
     * the screen is only composed when we're already in
     * [AuthState.Authenticated], so the null case is just the
     * first-frame fallback.
     */
    val instagramUsername: StateFlow<String?> = observeAuthState()
        .map { (it as? AuthState.Authenticated)?.session?.username }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = null,
        )

    private val _snackbarEvents = Channel<SnackbarEvent>(
        capacity = Channel.BUFFERED,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val snackbarEvents: Flow<SnackbarEvent> = _snackbarEvents.receiveAsFlow()

    init {
        refresh()
    }

    // ---------- sync ----------

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

    // ---------- tile gestures ----------

    /**
     * Single source of truth for tile clicks. Branches on the current
     * status: Available opens the shipping form; ReadyToShip and Shipped
     * open the read-only info sheet (which itself surfaces the transition
     * actions).
     */
    fun onProductClicked(product: Product) {
        if (transitionState.value.pending != null) return
        val pending = when (product.status) {
            ProductStatus.Available -> PendingTransition.CollectingShipping(
                productId = product.id.raw,
                form = ShippingFormState(),
            )
            is ProductStatus.ReadyToShip,
            is ProductStatus.Shipped,
            -> PendingTransition.ViewingInfo(productId = product.id.raw)
        }
        transitionState.update { it.copy(pending = pending, transitionError = null) }
    }

    fun onPendingTransitionDismissed() {
        transitionState.update { it.copy(pending = null) }
    }

    // ---------- shipping form ----------

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
        val sheet = transitionState.value.pending as? PendingTransition.CollectingShipping ?: return
        if (sheet.isSubmitting) return

        val validated = validate(sheet.form)
        if (validated != null) {
            transitionState.update {
                it.copy(pending = sheet.copy(form = validated))
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

        transitionState.update { it.copy(pending = sheet.copy(isSubmitting = true)) }

        viewModelScope.launch {
            val priorProduct = productOf(sheet.productId)
            val result = markReadyToShip(ProductId(sheet.productId), shipping)
            handleTransitionResult(result, priorProduct, SnackbarMessage.MarkedReadyToShip)
        }
    }

    // ---------- info-sheet actions ----------

    fun onShipFromInfoSheet() {
        val info = transitionState.value.pending as? PendingTransition.ViewingInfo ?: return
        transitionState.update { it.copy(pending = null) }
        viewModelScope.launch {
            val priorProduct = productOf(info.productId)
            val result = markShipped(ProductId(info.productId))
            handleTransitionResult(result, priorProduct, SnackbarMessage.Shipped)
        }
    }

    /**
     * Final-step revert from the info sheet's inline confirmation. The
     * sheet's local Compose state handles the "do you really mean it?"
     * prompt; by the time these run the user has already tapped
     * "Так, скасувати".
     */
    fun onRevertToAvailable() {
        val info = transitionState.value.pending as? PendingTransition.ViewingInfo ?: return
        transitionState.update { it.copy(pending = null) }
        viewModelScope.launch {
            val priorProduct = productOf(info.productId)
            val result = revertToAvailable(ProductId(info.productId))
            handleTransitionResult(result, priorProduct, SnackbarMessage.RevertedToAvailable)
        }
    }

    fun onRevertToReadyToShip() {
        val info = transitionState.value.pending as? PendingTransition.ViewingInfo ?: return
        transitionState.update { it.copy(pending = null) }
        viewModelScope.launch {
            val priorProduct = productOf(info.productId)
            val result = revertToReadyToShip(ProductId(info.productId))
            handleTransitionResult(result, priorProduct, SnackbarMessage.RevertedToReadyToShip)
        }
    }

    // ---------- undo ----------

    fun onUndo(snapshot: Product) {
        viewModelScope.launch {
            // Undo is best-effort; if the restore itself fails, swallow.
            // It would be jarring to show another snackbar about a failed
            // undo of a successful original action.
            restoreProduct(snapshot)
        }
    }

    // ---------- internals ----------

    private fun toUiState(
        products: List<Product>,
        sync: SyncStatus,
        transition: TransitionState,
    ): HomeUiState = when {
        !sync.hasSucceededOnce && sync.isSyncing && products.isEmpty() -> HomeUiState.InitialSync
        !sync.hasSucceededOnce && sync.error != null && products.isEmpty() ->
            HomeUiState.InitialSyncFailed(sync.error)
        else -> HomeUiState.Content(
            products = products,
            isRefreshing = sync.isSyncing,
            refreshError = if (sync.hasSucceededOnce) sync.error else null,
            pendingTransition = transition.pending,
            transitionError = transition.transitionError,
        )
    }

    private fun updateForm(transform: (ShippingFormState) -> ShippingFormState) {
        transitionState.update { current ->
            val sheet = current.pending as? PendingTransition.CollectingShipping ?: return@update current
            current.copy(pending = sheet.copy(form = transform(sheet.form)))
        }
    }

    private fun validate(form: ShippingFormState): ShippingFormState? {
        val fullNameError = if (form.fullName.isBlank()) ValidationMarker else null
        val phoneError = if (form.phone.isBlank()) ValidationMarker else null
        val cityError = if (form.city.isBlank()) ValidationMarker else null
        val pickupNumberError = if (form.pickupNumber.isBlank()) ValidationMarker else null
        return if (
            fullNameError == null && phoneError == null &&
            cityError == null && pickupNumberError == null
        ) null
        else form.copy(
            fullNameError = fullNameError,
            phoneError = phoneError,
            cityError = cityError,
            pickupNumberError = pickupNumberError,
        )
    }

    private fun productOf(id: String): Product? =
        (state.value as? HomeUiState.Content)?.products?.firstOrNull { it.id.raw == id }

    private fun handleTransitionResult(
        result: DomainResult<Product>,
        priorProduct: Product?,
        snackbarMessage: SnackbarMessage,
    ) {
        when (result) {
            is DomainResult.Success -> {
                transitionState.update { it.copy(pending = null, transitionError = null) }
                if (priorProduct != null) {
                    _snackbarEvents.trySend(
                        SnackbarEvent(message = snackbarMessage, undoSnapshot = priorProduct),
                    )
                }
            }
            is DomainResult.Failure -> transitionState.update {
                it.copy(pending = null, transitionError = result.error.toUserMessage())
            }
        }
    }

    private data class SyncStatus(
        val isSyncing: Boolean = false,
        val hasSucceededOnce: Boolean = false,
        val error: String? = null,
    )

    private data class TransitionState(
        val pending: PendingTransition? = null,
        val transitionError: String? = null,
    )

    private companion object {
        const val ValidationMarker = "REQUIRED"
    }
}

/**
 * One-shot snackbar payload. The screen renders the message and, on tap
 * of the action button, calls [HomeViewModel.onUndo] with the snapshot.
 */
data class SnackbarEvent(
    val message: SnackbarMessage,
    val undoSnapshot: Product,
)

enum class SnackbarMessage {
    MarkedReadyToShip,
    Shipped,
    RevertedToAvailable,
    RevertedToReadyToShip,
}

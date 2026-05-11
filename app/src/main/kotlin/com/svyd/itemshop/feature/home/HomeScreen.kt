package com.svyd.itemshop.feature.home

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import com.svyd.itemshop.R
import com.svyd.itemshop.domain.products.PickupPoint
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ShippingDetails
import com.svyd.itemshop.ui.components.ProductCoverImage
import com.svyd.itemshop.ui.components.StatusBadge
import com.svyd.itemshop.ui.format.formatForDisplay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val instagramUsername by viewModel.instagramUsername.collectAsStateWithLifecycle()
    HomeScreenContent(
        state = state,
        instagramUsername = instagramUsername,
        snackbarEvents = viewModel.snackbarEvents,
        onProductClick = viewModel::onProductClicked,
        onPendingTransitionDismissed = viewModel::onPendingTransitionDismissed,
        onShippingFullNameChange = viewModel::onShippingFullNameChange,
        onShippingPhoneChange = viewModel::onShippingPhoneChange,
        onShippingCityChange = viewModel::onShippingCityChange,
        onShippingPickupTypeChange = viewModel::onShippingPickupTypeChange,
        onShippingPickupNumberChange = viewModel::onShippingPickupNumberChange,
        onShippingSubmit = viewModel::onShippingSubmit,
        onShipFromInfoSheet = viewModel::onShipFromInfoSheet,
        onRevertToAvailable = viewModel::onRevertToAvailable,
        onRevertToReadyToShip = viewModel::onRevertToReadyToShip,
        onUndo = viewModel::onUndo,
        onSignOutClick = onSignOutClick,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenContent(
    state: HomeUiState,
    instagramUsername: String?,
    snackbarEvents: Flow<SnackbarEvent>,
    onProductClick: (Product) -> Unit,
    onPendingTransitionDismissed: () -> Unit,
    onShippingFullNameChange: (String) -> Unit,
    onShippingPhoneChange: (String) -> Unit,
    onShippingCityChange: (String) -> Unit,
    onShippingPickupTypeChange: (PickupTypeOption) -> Unit,
    onShippingPickupNumberChange: (String) -> Unit,
    onShippingSubmit: () -> Unit,
    onShipFromInfoSheet: () -> Unit,
    onRevertToAvailable: () -> Unit,
    onRevertToReadyToShip: () -> Unit,
    onUndo: (Product) -> Unit,
    onSignOutClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.action_undo)
    val markedReadyMessage = stringResource(R.string.snackbar_marked_ready_to_ship)
    val shippedMessage = stringResource(R.string.snackbar_shipped)
    val revertedToAvailableMessage = stringResource(R.string.snackbar_reverted_to_available)
    val revertedToReadyMessage = stringResource(R.string.snackbar_reverted_to_ready_to_ship)

    LaunchedEffect(snackbarEvents) {
        snackbarEvents.collect { event ->
            // Dismiss any in-flight snackbar so the user always sees the
            // latest transition's undo opportunity; channel-backed flows
            // deliver each event to exactly one collector, so this and
            // the showSnackbar call below must live in the same block.
            snackbarHostState.currentSnackbarData?.dismiss()
            val message = when (event.message) {
                SnackbarMessage.MarkedReadyToShip -> markedReadyMessage
                SnackbarMessage.Shipped -> shippedMessage
                SnackbarMessage.RevertedToAvailable -> revertedToAvailableMessage
                SnackbarMessage.RevertedToReadyToShip -> revertedToReadyMessage
            }
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
                withDismissAction = false,
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndo(event.undoSnapshot)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    // Show the signed-in user's Instagram handle. While
                    // the auth observation is resolving on first frame
                    // (basically never reaches the user on a real device
                    // since the AuthGate above already saw Authenticated),
                    // fall back to the static brand label.
                    Text(instagramUsername ?: stringResource(R.string.home_title))
                },
                actions = {
                    if (state is HomeUiState.Content && state.isRefreshing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp),
                        )
                    }
                    HomeOverflowMenu(onSignOutClick = onSignOutClick)
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { padding ->
        when (state) {
            HomeUiState.InitialSync -> CenteredMessage(
                message = stringResource(R.string.home_first_sync_in_progress),
                contentPadding = padding,
                showProgress = true,
            )
            is HomeUiState.InitialSyncFailed -> InitialFailure(
                message = state.message,
                onRetry = onRefresh,
                contentPadding = padding,
            )
            is HomeUiState.Content -> {
                ContentBody(
                    state = state,
                    onProductClick = onProductClick,
                    onRefresh = onRefresh,
                    contentPadding = padding,
                )
                PendingTransitionOverlay(
                    state = state,
                    onDismiss = onPendingTransitionDismissed,
                    onShippingFullNameChange = onShippingFullNameChange,
                    onShippingPhoneChange = onShippingPhoneChange,
                    onShippingCityChange = onShippingCityChange,
                    onShippingPickupTypeChange = onShippingPickupTypeChange,
                    onShippingPickupNumberChange = onShippingPickupNumberChange,
                    onShippingSubmit = onShippingSubmit,
                    onShipFromInfoSheet = onShipFromInfoSheet,
                    onRevertToAvailable = onRevertToAvailable,
                    onRevertToReadyToShip = onRevertToReadyToShip,
                )
            }
        }
    }
}

@Composable
private fun HomeOverflowMenu(onSignOutClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.products_logout)) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                onClick = {
                    expanded = false
                    onSignOutClick()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentBody(
    state: HomeUiState.Content,
    onProductClick: (Product) -> Unit,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = {
            scope.launch { pullToRefreshState.animateToHidden() }
            onRefresh()
        },
        state = pullToRefreshState,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            state.refreshError?.let { msg ->
                RefreshErrorBanner(message = msg)
            }
            state.transitionError?.let { msg ->
                RefreshErrorBanner(message = msg)
            }
            when {
                state.products.isEmpty() -> EmptyContent()
                else -> ProductsGrid(
                    products = state.products,
                    onProductClick = onProductClick,
                )
            }
        }
    }
}

@Composable
private fun ProductsGrid(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = products, key = { it.id.raw }) { product ->
            ProductTile(product = product, onClick = { onProductClick(product) })
        }
    }
}

@Composable
private fun ProductTile(product: Product, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        ProductCoverImage(
            product = product,
            modifier = Modifier.fillMaxSize(),
        )

        // Vertical scrim across the whole tile — transparent at the top,
        // opaque at the bottom — so the name label at the bottom-left
        // reads against any cover image without obscuring the photo
        // itself. Sits between the image and the labels.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                        ),
                    ),
                ),
        )

        // Top-right: price on Available tiles (if present), status badge
        // on non-Available tiles.
        if (product.status !is ProductStatus.Available) {
            StatusBadge(
                status = product.status,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            )
        }

        // Bottom-left: name, always shown.
        TileCornerLabel(
            text = product.title,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
        )
    }
}

/**
 * Compact white-on-scrim label used in tile corners. Same text style for
 * both the name (bottom-left) and the price (top-right of Available tiles)
 * so they read as a pair.
 */
@Composable
private fun TileCornerLabel(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RefreshErrorBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun CenteredMessage(
    message: String,
    contentPadding: PaddingValues,
    showProgress: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showProgress) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InitialFailure(
    message: String,
    onRetry: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_first_sync_failed),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

// ---------- pending transition overlays ----------

@Composable
private fun PendingTransitionOverlay(
    state: HomeUiState.Content,
    onDismiss: () -> Unit,
    onShippingFullNameChange: (String) -> Unit,
    onShippingPhoneChange: (String) -> Unit,
    onShippingCityChange: (String) -> Unit,
    onShippingPickupTypeChange: (PickupTypeOption) -> Unit,
    onShippingPickupNumberChange: (String) -> Unit,
    onShippingSubmit: () -> Unit,
    onShipFromInfoSheet: () -> Unit,
    onRevertToAvailable: () -> Unit,
    onRevertToReadyToShip: () -> Unit,
) {
    when (val pending = state.pendingTransition) {
        null -> Unit
        is PendingTransition.CollectingShipping -> ShippingSheet(
            pending = pending,
            onDismiss = onDismiss,
            onFullNameChange = onShippingFullNameChange,
            onPhoneChange = onShippingPhoneChange,
            onCityChange = onShippingCityChange,
            onPickupTypeChange = onShippingPickupTypeChange,
            onPickupNumberChange = onShippingPickupNumberChange,
            onSubmit = onShippingSubmit,
        )
        is PendingTransition.ViewingInfo -> {
            val product = state.products.firstOrNull { it.id.raw == pending.productId }
            if (product != null) {
                InfoSheet(
                    product = product,
                    onDismiss = onDismiss,
                    onShip = onShipFromInfoSheet,
                    onRevertToAvailable = onRevertToAvailable,
                    onRevertToReadyToShip = onRevertToReadyToShip,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShippingSheet(
    pending: PendingTransition.CollectingShipping,
    onDismiss: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPickupTypeChange: (PickupTypeOption) -> Unit,
    onPickupNumberChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.shipping_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            FilledFormField(
                label = stringResource(R.string.shipping_field_full_name),
                value = pending.form.fullName,
                onValueChange = onFullNameChange,
                placeholder = stringResource(R.string.shipping_field_full_name_placeholder),
                isError = pending.form.fullNameError != null,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledFormField(
                    label = stringResource(R.string.shipping_field_phone),
                    value = pending.form.phone,
                    onValueChange = onPhoneChange,
                    placeholder = stringResource(R.string.shipping_field_phone_placeholder),
                    isError = pending.form.phoneError != null,
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.weight(1f),
                )
                FilledFormField(
                    label = stringResource(R.string.shipping_field_city),
                    value = pending.form.city,
                    onValueChange = onCityChange,
                    placeholder = stringResource(R.string.shipping_field_city_placeholder),
                    isError = pending.form.cityError != null,
                    modifier = Modifier.weight(1f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.shipping_field_pickup_type),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    PickupTypeOption.entries.forEach { option ->
                        PickupRadio(
                            selected = pending.form.pickupType == option,
                            onClick = { onPickupTypeChange(option) },
                            label = stringResource(
                                when (option) {
                                    PickupTypeOption.PostOffice -> R.string.shipping_pickup_post_office
                                    PickupTypeOption.ParcelLocker -> R.string.shipping_pickup_parcel_locker
                                },
                            ),
                        )
                    }
                }
                FilledFormField(
                    label = null,
                    value = pending.form.pickupNumber,
                    onValueChange = onPickupNumberChange,
                    placeholder = stringResource(R.string.shipping_field_pickup_number),
                    isError = pending.form.pickupNumberError != null,
                    keyboardType = KeyboardType.Number,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = onSubmit,
                    enabled = !pending.isSubmitting,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Text(stringResource(R.string.shipping_save))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FilledFormField(
    label: String?,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            supportingText = if (isError) {
                { Text(stringResource(R.string.error_required_field)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PickupRadio(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.selectable(selected = selected, onClick = onClick),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoSheet(
    product: Product,
    onDismiss: () -> Unit,
    onShip: () -> Unit,
    onRevertToAvailable: () -> Unit,
    onRevertToReadyToShip: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val titleRes = when (product.status) {
        is ProductStatus.ReadyToShip -> R.string.info_sheet_title_ready_to_ship
        is ProductStatus.Shipped -> R.string.info_sheet_title_shipped
        // Unreachable: Available products don't open the info sheet.
        ProductStatus.Available -> R.string.info_sheet_title_ready_to_ship
    }

    // Local-only "do you really mean it?" prompt. Lives here rather than
    // in the VM because it has no meaning outside the sheet's lifetime:
    // if the sheet dismisses, the question dies with it. Reset whenever
    // the underlying product id changes (e.g. info sheet re-opened for a
    // different product without intermediate dismiss).
    var revertConfirmation by remember(product.id.raw) {
        mutableStateOf<RevertTarget?>(null)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            InfoSheetHeader(product = product)

            shippingDetailsOf(product.status)?.let { shipping ->
                ShippingDetailsCard(shipping = shipping)
            }

            AnimatedContent(
                targetState = revertConfirmation,
                label = "info-sheet-bottom",
            ) { target ->
                when (target) {
                    null -> InfoSheetActions(
                        status = product.status,
                        onShip = onShip,
                        onRevertToAvailable = { revertConfirmation = RevertTarget.Available },
                        onRevertToReadyToShip = { revertConfirmation = RevertTarget.ReadyToShip },
                    )
                    RevertTarget.Available -> RevertConfirmation(
                        titleRes = R.string.revert_to_available_title,
                        messageRes = R.string.revert_to_available_message,
                        onConfirm = onRevertToAvailable,
                        onCancel = { revertConfirmation = null },
                    )
                    RevertTarget.ReadyToShip -> RevertConfirmation(
                        titleRes = R.string.revert_to_ready_to_ship_title,
                        messageRes = R.string.revert_to_ready_to_ship_message,
                        onConfirm = onRevertToReadyToShip,
                        onCancel = { revertConfirmation = null },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private enum class RevertTarget { Available, ReadyToShip }

@Composable
private fun RevertConfirmation(
    titleRes: Int,
    messageRes: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(titleRes) + " " + stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Text(stringResource(R.string.alert_revert_dismiss))
                }
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Text(stringResource(R.string.alert_revert_confirm))
                }
            }
        }
    }
}

@Composable
private fun InfoSheetHeader(product: Product) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProductCoverImage(
            product = product,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            product.price?.let { price ->
                Spacer(Modifier.height(2.dp))
                Text(
                    text = price.formatForDisplay(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ShippingDetailsCard(shipping: ShippingDetails) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShippingDetailRow(
                icon = Icons.Outlined.Person,
                text = shipping.fullName,
            )
            ShippingDetailRow(
                icon = Icons.Outlined.Phone,
                text = shipping.phone,
            )
            ShippingDetailRow(
                icon = Icons.Outlined.LocationOn,
                text = shipping.city,
                subText = pickupPointLabel(shipping.pickupPoint),
            )
        }
    }
}

@Composable
private fun ShippingDetailRow(
    icon: ImageVector,
    text: String,
    subText: String? = null,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 1.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
            subText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoSheetActions(
    status: ProductStatus,
    onShip: () -> Unit,
    onRevertToAvailable: () -> Unit,
    onRevertToReadyToShip: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (status) {
            is ProductStatus.ReadyToShip -> {
                Button(
                    onClick = onShip,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_ship))
                }
                OutlinedButton(
                    onClick = onRevertToAvailable,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(stringResource(R.string.action_revert_to_available))
                }
            }
            is ProductStatus.Shipped -> {
                OutlinedButton(
                    onClick = onRevertToReadyToShip,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(stringResource(R.string.action_revert_to_ready_to_ship))
                }
            }
            ProductStatus.Available -> Unit
        }
    }
}

@Composable
private fun pickupPointLabel(pickup: PickupPoint): String {
    val typeRes = when (pickup) {
        is PickupPoint.PostOffice -> R.string.shipping_pickup_post_office
        is PickupPoint.ParcelLocker -> R.string.shipping_pickup_parcel_locker
    }
    return "${stringResource(typeRes)} #${pickup.number}"
}

private fun shippingDetailsOf(status: ProductStatus): ShippingDetails? = when (status) {
    ProductStatus.Available -> null
    is ProductStatus.ReadyToShip -> status.shipping
    is ProductStatus.Shipped -> status.shipping
}


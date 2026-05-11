package com.svyd.itemshop.feature.products.details

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svyd.itemshop.R
import com.svyd.itemshop.domain.products.PickupPoint
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ShippingDetails
import com.svyd.itemshop.ui.components.ProductCoverImage
import com.svyd.itemshop.ui.components.StatusBadge
import com.svyd.itemshop.ui.format.formatForDisplay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductDetailsScreen(
    id: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailsViewModel = koinViewModel(
        parameters = { parametersOf(id) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProductDetailsScreen(
        state = state,
        onBackClick = onBackClick,
        onPrimaryActionClick = viewModel::onPrimaryActionClick,
        onRevertToAvailableClick = viewModel::onRevertToAvailableClick,
        onRevertToReadyToShipClick = viewModel::onRevertToReadyToShipClick,
        onRevertConfirmed = viewModel::onRevertConfirmed,
        onPendingTransitionDismissed = viewModel::onPendingTransitionDismissed,
        onShippingFullNameChange = viewModel::onShippingFullNameChange,
        onShippingPhoneChange = viewModel::onShippingPhoneChange,
        onShippingCityChange = viewModel::onShippingCityChange,
        onShippingPickupTypeChange = viewModel::onShippingPickupTypeChange,
        onShippingPickupNumberChange = viewModel::onShippingPickupNumberChange,
        onShippingSubmit = viewModel::onShippingSubmit,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProductDetailsScreen(
    state: ProductDetailsUiState,
    onBackClick: () -> Unit,
    onPrimaryActionClick: () -> Unit,
    onRevertToAvailableClick: () -> Unit,
    onRevertToReadyToShipClick: () -> Unit,
    onRevertConfirmed: () -> Unit,
    onPendingTransitionDismissed: () -> Unit,
    onShippingFullNameChange: (String) -> Unit,
    onShippingPhoneChange: (String) -> Unit,
    onShippingCityChange: (String) -> Unit,
    onShippingPickupTypeChange: (PickupTypeOption) -> Unit,
    onShippingPickupNumberChange: (String) -> Unit,
    onShippingSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state is ProductDetailsUiState.Content) {
                        OverflowMenu(
                            status = state.product.status,
                            onRevertToAvailableClick = onRevertToAvailableClick,
                            onRevertToReadyToShipClick = onRevertToReadyToShipClick,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            ProductDetailsUiState.Loading -> CenteredProgress(padding)
            ProductDetailsUiState.NotFound -> CenteredMessage(
                message = stringResource(R.string.error_unknown),
                contentPadding = padding,
            )
            is ProductDetailsUiState.Content -> {
                ProductBody(
                    product = state.product,
                    transitionError = state.transitionError,
                    onPrimaryActionClick = onPrimaryActionClick,
                    contentPadding = padding,
                )
                when (val pending = state.pendingTransition) {
                    null -> Unit
                    is PendingTransition.CollectingShipping -> ShippingSheet(
                        pending = pending,
                        onDismiss = onPendingTransitionDismissed,
                        onFullNameChange = onShippingFullNameChange,
                        onPhoneChange = onShippingPhoneChange,
                        onCityChange = onShippingCityChange,
                        onPickupTypeChange = onShippingPickupTypeChange,
                        onPickupNumberChange = onShippingPickupNumberChange,
                        onSubmit = onShippingSubmit,
                    )
                    PendingTransition.ConfirmRevertToAvailable -> RevertDialog(
                        titleRes = R.string.revert_to_available_title,
                        messageRes = R.string.revert_to_available_message,
                        onConfirm = onRevertConfirmed,
                        onDismiss = onPendingTransitionDismissed,
                    )
                    PendingTransition.ConfirmRevertToReadyToShip -> RevertDialog(
                        titleRes = R.string.revert_to_ready_to_ship_title,
                        messageRes = R.string.revert_to_ready_to_ship_message,
                        onConfirm = onRevertConfirmed,
                        onDismiss = onPendingTransitionDismissed,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverflowMenu(
    status: ProductStatus,
    onRevertToAvailableClick: () -> Unit,
    onRevertToReadyToShipClick: () -> Unit,
) {
    // Only show the overflow icon when at least one revert action applies
    // (i.e. status isn't Available).
    if (status is ProductStatus.Available) return
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            when (status) {
                is ProductStatus.ReadyToShip -> DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_revert_to_available)) },
                    onClick = { expanded = false; onRevertToAvailableClick() },
                )
                is ProductStatus.Shipped -> DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_revert_to_ready_to_ship)) },
                    onClick = { expanded = false; onRevertToReadyToShipClick() },
                )
                ProductStatus.Available -> Unit
            }
        }
    }
}

@Composable
private fun ProductBody(
    product: Product,
    transitionError: String?,
    onPrimaryActionClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProductCoverImage(
            product = product,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
        )

        Text(text = product.title, style = MaterialTheme.typography.headlineSmall)

        product.price?.let { price ->
            Text(
                text = price.formatForDisplay(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        StatusBadge(status = product.status)

        PrimaryActionButton(status = product.status, onClick = onPrimaryActionClick)

        transitionError?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        shippingDetailsOf(product.status)?.let { shipping ->
            HorizontalDivider()
            ShippingSection(shipping = shipping)
        }
    }
}

@Composable
private fun PrimaryActionButton(status: ProductStatus, onClick: () -> Unit) {
    val labelRes = when (status) {
        ProductStatus.Available -> R.string.action_mark_ready_to_ship
        is ProductStatus.ReadyToShip -> R.string.action_ship
        is ProductStatus.Shipped -> return // No primary action for Shipped.
    }
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(labelRes))
    }
}

@Composable
private fun ShippingSection(shipping: ShippingDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.product_details_shipping_section_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = shipping.fullName, style = MaterialTheme.typography.bodyLarge)
        Text(text = shipping.phone, style = MaterialTheme.typography.bodyMedium)
        Text(text = shipping.city, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = pickupPointLabel(shipping.pickupPoint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.shipping_sheet_title),
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = pending.form.fullName,
                onValueChange = onFullNameChange,
                label = { Text(stringResource(R.string.shipping_field_full_name)) },
                singleLine = true,
                isError = pending.form.fullNameError != null,
                supportingText = pending.form.fullNameError?.let {
                    { Text(stringResource(R.string.error_required_field)) }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = pending.form.phone,
                onValueChange = onPhoneChange,
                label = { Text(stringResource(R.string.shipping_field_phone)) },
                singleLine = true,
                isError = pending.form.phoneError != null,
                supportingText = pending.form.phoneError?.let {
                    { Text(stringResource(R.string.error_required_field)) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = pending.form.city,
                onValueChange = onCityChange,
                label = { Text(stringResource(R.string.shipping_field_city)) },
                singleLine = true,
                isError = pending.form.cityError != null,
                supportingText = pending.form.cityError?.let {
                    { Text(stringResource(R.string.error_required_field)) }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.shipping_field_pickup_type),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column {
                PickupTypeOption.entries.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = pending.form.pickupType == option,
                                onClick = { onPickupTypeChange(option) },
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = pending.form.pickupType == option,
                            onClick = { onPickupTypeChange(option) },
                        )
                        Text(
                            text = stringResource(
                                when (option) {
                                    PickupTypeOption.PostOffice -> R.string.shipping_pickup_post_office
                                    PickupTypeOption.ParcelLocker -> R.string.shipping_pickup_parcel_locker
                                },
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = pending.form.pickupNumber,
                onValueChange = onPickupNumberChange,
                label = { Text(stringResource(R.string.shipping_field_pickup_number)) },
                singleLine = true,
                isError = pending.form.pickupNumberError != null,
                supportingText = pending.form.pickupNumberError?.let {
                    { Text(stringResource(R.string.error_required_field)) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = onSubmit,
                    enabled = !pending.isSubmitting,
                ) {
                    Text(stringResource(R.string.shipping_save))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RevertDialog(
    titleRes: Int,
    messageRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(messageRes)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun CenteredProgress(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenteredMessage(message: String, contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
    }
}

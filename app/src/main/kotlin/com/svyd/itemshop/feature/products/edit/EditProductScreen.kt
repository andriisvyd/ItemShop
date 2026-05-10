package com.svyd.itemshop.feature.products.edit

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.svyd.itemshop.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditProductScreen(
    id: String,
    onSaved: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProductViewModel = koinViewModel(
        parameters = { parametersOf(id) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is EditProductUiState.Editing && (state as EditProductUiState.Editing).saved) {
            onSaved()
        }
    }

    EditProductScreen(
        state = state,
        onBackClick = onBackClick,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onPriceAmountChange = viewModel::onPriceAmountChange,
        onPriceCurrencyChange = viewModel::onPriceCurrencyChange,
        onSaveClick = viewModel::onSaveClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditProductScreen(
    state: EditProductUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceAmountChange: (String) -> Unit,
    onPriceCurrencyChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val titleRes = when {
                        state is EditProductUiState.Editing && state.mode == EditProductUiState.Mode.Edit ->
                            R.string.edit_product_title_edit
                        else -> R.string.edit_product_title_create
                    }
                    Text(stringResource(titleRes))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state is EditProductUiState.Editing) {
                        TextButton(
                            onClick = onSaveClick,
                            enabled = !state.isSaving,
                        ) {
                            Text(stringResource(R.string.edit_product_save))
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            EditProductUiState.Loading -> CenteredProgress(padding)
            is EditProductUiState.LoadFailed -> CenteredMessage(state.message, padding)
            is EditProductUiState.Editing -> EditingForm(
                state = state,
                onTitleChange = onTitleChange,
                onDescriptionChange = onDescriptionChange,
                onPriceAmountChange = onPriceAmountChange,
                onPriceCurrencyChange = onPriceCurrencyChange,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun EditingForm(
    state: EditProductUiState.Editing,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceAmountChange: (String) -> Unit,
    onPriceCurrencyChange: (String) -> Unit,
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
        state.form.coverImageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }

        OutlinedTextField(
            value = state.form.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.edit_product_field_title)) },
            isError = state.form.titleError != null,
            supportingText = state.form.titleError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.form.priceAmount,
                onValueChange = onPriceAmountChange,
                label = { Text(stringResource(R.string.edit_product_field_price)) },
                singleLine = true,
                isError = state.form.priceError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(2f),
            )
            OutlinedTextField(
                value = state.form.priceCurrency,
                onValueChange = onPriceCurrencyChange,
                label = { Text("Cur.") },
                singleLine = true,
                isError = state.form.priceError != null,
                modifier = Modifier.weight(1f),
            )
        }
        state.form.priceError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        OutlinedTextField(
            value = state.form.description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.edit_product_field_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        )

        state.saveError?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (state.isSaving) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

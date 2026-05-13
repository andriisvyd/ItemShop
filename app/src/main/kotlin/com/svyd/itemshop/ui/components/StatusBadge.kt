package com.svyd.itemshop.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.svyd.itemshop.R
import com.svyd.itemshop.domain.products.ProductStatus

/**
 * Compact status pill. Used both as a top-right overlay on product list
 * cards and as an inline badge on the details screen.
 */
@Composable
fun StatusBadge(
    status: ProductStatus,
    modifier: Modifier = Modifier,
) {
    val labelRes = when (status) {
        ProductStatus.Available -> R.string.status_available
        is ProductStatus.ReadyToShip -> R.string.status_ready_to_ship
        is ProductStatus.Shipped -> R.string.status_shipped
    }
    val (container, content) = statusColors(status)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun statusColors(status: ProductStatus): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        ProductStatus.Available -> scheme.surfaceVariant to scheme.onSurfaceVariant
        is ProductStatus.ReadyToShip -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        is ProductStatus.Shipped -> scheme.primaryContainer to scheme.onPrimaryContainer
    }
}

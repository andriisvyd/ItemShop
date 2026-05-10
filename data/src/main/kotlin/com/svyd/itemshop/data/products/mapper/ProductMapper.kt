package com.svyd.itemshop.data.products.mapper

import com.svyd.itemshop.data.products.local.ProductEntity
import com.svyd.itemshop.domain.products.CurrencySymbol
import com.svyd.itemshop.domain.products.Price
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import kotlinx.datetime.Instant

internal fun ProductEntity.toDomain(): Product = Product(
    id = ProductId(id),
    title = title,
    description = description,
    price = composePrice(priceAmountMinor, priceCurrencySymbol),
    coverImageUrl = coverImageUrl,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs),
    updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMs),
)

internal fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id.raw,
    title = title,
    description = description,
    priceAmountMinor = price?.amountMinor,
    priceCurrencySymbol = price?.currency?.raw,
    coverImageUrl = coverImageUrl,
    createdAtEpochMs = createdAt.toEpochMilliseconds(),
    updatedAtEpochMs = updatedAt.toEpochMilliseconds(),
)

private fun composePrice(amountMinor: Long?, currencySymbol: String?): Price? {
    if (amountMinor == null || currencySymbol == null) return null
    return runCatching {
        Price(amountMinor = amountMinor, currency = CurrencySymbol(currencySymbol))
    }.getOrNull()
}

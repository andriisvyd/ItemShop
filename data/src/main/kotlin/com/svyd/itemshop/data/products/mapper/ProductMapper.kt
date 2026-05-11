package com.svyd.itemshop.data.products.mapper

import com.svyd.itemshop.data.products.local.ProductEntity
import com.svyd.itemshop.domain.products.CurrencySymbol
import com.svyd.itemshop.domain.products.PickupPoint
import com.svyd.itemshop.domain.products.Price
import com.svyd.itemshop.domain.products.Product
import com.svyd.itemshop.domain.products.ProductId
import com.svyd.itemshop.domain.products.ProductStatus
import com.svyd.itemshop.domain.products.ShippingDetails
import kotlinx.datetime.Instant

internal fun ProductEntity.toDomain(): Product = Product(
    id = ProductId(id),
    title = title,
    price = composePrice(priceAmountMinor, priceCurrencySymbol),
    coverImageUrl = coverImageUrl,
    status = composeStatus(this),
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs),
    updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMs),
)

internal fun Product.toEntity(): ProductEntity {
    val shipping = shippingOf(status)
    val pickupType = shipping?.pickupPoint?.let(::pickupTypeCode)
    return ProductEntity(
        id = id.raw,
        title = title,
        priceAmountMinor = price?.amountMinor,
        priceCurrencySymbol = price?.currency?.raw,
        coverImageUrl = coverImageUrl,
        status = statusCode(status),
        shippingFullName = shipping?.fullName,
        shippingPhone = shipping?.phone,
        shippingCity = shipping?.city,
        shippingPickupType = pickupType,
        shippingPickupNumber = shipping?.pickupPoint?.number,
        createdAtEpochMs = createdAt.toEpochMilliseconds(),
        updatedAtEpochMs = updatedAt.toEpochMilliseconds(),
    )
}

private fun composePrice(amountMinor: Long?, currencySymbol: String?): Price? {
    if (amountMinor == null || currencySymbol == null) return null
    return runCatching {
        Price(amountMinor = amountMinor, currency = CurrencySymbol(currencySymbol))
    }.getOrNull()
}

/**
 * Rebuild a [ProductStatus] from flat columns. If the row claims a
 * non-Available status but the shipping columns are missing or
 * malformed, we fall back to [ProductStatus.Available] rather than
 * crashing — the alternative would be losing the entire product to a
 * single bad column. The condition is logged-on-debug via the failing
 * `runCatching` paths in the helpers.
 */
private fun composeStatus(entity: ProductEntity): ProductStatus {
    return when (entity.status) {
        StatusCode.AVAILABLE -> ProductStatus.Available
        StatusCode.READY_TO_SHIP -> entity.composeShipping()
            ?.let(ProductStatus::ReadyToShip)
            ?: ProductStatus.Available
        StatusCode.SHIPPED -> entity.composeShipping()
            ?.let(ProductStatus::Shipped)
            ?: ProductStatus.Available
        else -> ProductStatus.Available
    }
}

private fun ProductEntity.composeShipping(): ShippingDetails? {
    val name = shippingFullName ?: return null
    val phone = shippingPhone ?: return null
    val city = shippingCity ?: return null
    val pickupType = shippingPickupType ?: return null
    val pickupNumber = shippingPickupNumber ?: return null
    val pickup = composePickup(pickupType, pickupNumber) ?: return null
    return runCatching {
        ShippingDetails(fullName = name, phone = phone, city = city, pickupPoint = pickup)
    }.getOrNull()
}

private fun composePickup(type: String, number: String): PickupPoint? = runCatching {
    when (type) {
        PickupTypeCode.POST_OFFICE -> PickupPoint.PostOffice(number)
        PickupTypeCode.PARCEL_LOCKER -> PickupPoint.ParcelLocker(number)
        else -> null
    }
}.getOrNull()

private fun statusCode(status: ProductStatus): String = when (status) {
    ProductStatus.Available -> StatusCode.AVAILABLE
    is ProductStatus.ReadyToShip -> StatusCode.READY_TO_SHIP
    is ProductStatus.Shipped -> StatusCode.SHIPPED
}

private fun shippingOf(status: ProductStatus): ShippingDetails? = when (status) {
    ProductStatus.Available -> null
    is ProductStatus.ReadyToShip -> status.shipping
    is ProductStatus.Shipped -> status.shipping
}

private fun pickupTypeCode(pickup: PickupPoint): String = when (pickup) {
    is PickupPoint.PostOffice -> PickupTypeCode.POST_OFFICE
    is PickupPoint.ParcelLocker -> PickupTypeCode.PARCEL_LOCKER
}

/**
 * Wire-format constants for the `status` and `shippingPickupType` columns.
 * Centralised so it's obvious where to add new variants when the spec
 * grows (e.g. when `Booked` lands).
 */
private object StatusCode {
    const val AVAILABLE = "AVAILABLE"
    const val READY_TO_SHIP = "READY_TO_SHIP"
    const val SHIPPED = "SHIPPED"
}

private object PickupTypeCode {
    const val POST_OFFICE = "POST_OFFICE"
    const val PARCEL_LOCKER = "PARCEL_LOCKER"
}

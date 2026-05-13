package com.svyd.itemshop.data.products.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room representation of a product. The primary key is the originating
 * Instagram media id — there is no separate `sourcePostId` foreign key
 * because every product is a 1:1 extension of a post.
 *
 * Price is stored as two nullable columns; the mapper composes them back
 * into a domain `Price?`. Both columns must be non-null together or both
 * null (validated in the mapper).
 *
 * Status is stored as a string (see `ProductStatusCode` constants in the
 * mapper). Shipping columns are nullable because Available products carry
 * no shipping info; the domain `ShippingDetails` type still requires all
 * fields, so the mapper enforces "either every shipping column is set
 * together with a non-Available status, or all are null".
 */
@Entity(tableName = "products")
internal data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val priceAmountMinor: Long?,
    val priceCurrencySymbol: String?,
    val coverImageUrl: String?,
    val status: String,
    val shippingFullName: String?,
    val shippingPhone: String?,
    val shippingCity: String?,
    val shippingPickupType: String?,
    val shippingPickupNumber: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

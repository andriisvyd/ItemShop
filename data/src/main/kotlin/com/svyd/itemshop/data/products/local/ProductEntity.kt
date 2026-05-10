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
 */
@Entity(tableName = "products")
internal data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceAmountMinor: Long?,
    val priceCurrencySymbol: String?,
    val coverImageUrl: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

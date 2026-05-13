package com.svyd.itemshop.domain.products

import kotlinx.datetime.Instant

/**
 * A product entity in our shop. Identity is the originating Instagram media
 * id — every product is, by design, a 1:1 extension of an existing post.
 * No locally-generated ids and no separate `sourcePostId` foreign key.
 */
data class Product(
    val id: ProductId,
    val title: String,
    val price: Price?,
    val coverImageUrl: String?,
    val status: ProductStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@JvmInline
value class ProductId(val raw: String) {
    init {
        require(raw.isNotBlank()) { "ProductId must not be blank" }
    }
}

package com.svyd.itemshop.domain.products

/**
 * In-flight, possibly invalid product data being edited in the UI.
 * `id` is always set — it equals the originating Instagram post id and is
 * fixed at draft creation time. The `Save` use case validates and
 * promotes a draft to a [Product].
 */
data class ProductDraft(
    val id: ProductId,
    val title: String = "",
    val description: String = "",
    val price: Price? = null,
    val coverImageUrl: String? = null,
)

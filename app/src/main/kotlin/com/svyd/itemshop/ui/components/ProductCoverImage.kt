package com.svyd.itemshop.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.svyd.itemshop.domain.products.Product

/**
 * Renders a product's cover image with **stable Coil cache keys** tied to
 * the product id rather than the (signed, rotating) Instagram CDN URL.
 *
 * Instagram returns a fresh URL for the same image on every `/me/media`
 * request — `oh` / `oe` query params (hash + expiration) rotate per call.
 * Coil keys its memory & disk caches by URL string by default, so every
 * sync would re-fetch the same bytes and cause visible flicker. Pinning
 * the cache to the product id keeps the cached bitmap stable across
 * syncs.
 *
 * Trade-off: if the underlying Instagram media bytes ever change (user
 * replaces the photo on an existing post — uncommon), this cache won't
 * pick up the new image until evicted.
 */
@Composable
fun ProductCoverImage(
    product: Product,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = product.title,
) {
    val url = product.coverImageUrl ?: return
    AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(url)
            .memoryCacheKey(product.id.raw)
            .diskCacheKey(product.id.raw)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

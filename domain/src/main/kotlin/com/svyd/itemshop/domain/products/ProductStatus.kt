package com.svyd.itemshop.domain.products

/**
 * The product's fulfilment status.
 *
 * Each non-empty status carries the data it implies, so impossible states
 * — e.g. a `Shipped` product without shipping details — cannot be
 * represented in the type system. Transitions are mediated by dedicated
 * use cases (`MarkReadyToShip`, `MarkShipped`, `RevertToReadyToShip`,
 * `RevertToAvailable`) that validate the current status and preserve
 * carried data where it makes sense (e.g. reverting `Shipped` →
 * `ReadyToShip` keeps the shipping info that was already collected).
 *
 * A `Booked` status is on the roadmap but intentionally left out of v1.
 */
sealed interface ProductStatus {

    data object Available : ProductStatus

    data class ReadyToShip(val shipping: ShippingDetails) : ProductStatus

    data class Shipped(val shipping: ShippingDetails) : ProductStatus
}

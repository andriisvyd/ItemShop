package com.svyd.itemshop.domain.products

/**
 * Buyer info collected when a product transitions to `ReadyToShip`.
 *
 * All fields are non-blank — the data class itself enforces the invariant
 * via `init` so the rest of the codebase can rely on it. The presentation
 * layer is expected to validate user input before constructing this type;
 * an `IllegalArgumentException` here would indicate a programmer bug
 * rather than a UI shortcoming.
 */
data class ShippingDetails(
    val fullName: String,
    val phone: String,
    val city: String,
    val pickupPoint: PickupPoint,
) {
    init {
        require(fullName.isNotBlank()) { "fullName must not be blank" }
        require(phone.isNotBlank()) { "phone must not be blank" }
        require(city.isNotBlank()) { "city must not be blank" }
    }
}

/**
 * Where the parcel is to be picked up. Modelled as a sealed hierarchy
 * (rather than enum + string) so the type system can later carry
 * variant-specific data (carrier hints, capacity, etc.) without
 * source-incompatible changes. Each variant currently carries the
 * branch / locker [number] as a free-form string.
 */
sealed interface PickupPoint {
    val number: String

    data class PostOffice(override val number: String) : PickupPoint {
        init { require(number.isNotBlank()) { "PostOffice number must not be blank" } }
    }

    data class ParcelLocker(override val number: String) : PickupPoint {
        init { require(number.isNotBlank()) { "ParcelLocker number must not be blank" } }
    }
}

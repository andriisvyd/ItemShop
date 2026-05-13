package com.svyd.itemshop.domain.common

sealed class DomainError(open val cause: Throwable? = null) {
    data class Network(override val cause: Throwable? = null) : DomainError(cause)
    data class Unauthorized(override val cause: Throwable? = null) : DomainError(cause)
    data class NotFound(val what: String) : DomainError()
    data class OAuthCancelled(override val cause: Throwable? = null) : DomainError(cause)
    data class OAuthDenied(val description: String?) : DomainError()
    data class Validation(val field: String, val reason: String) : DomainError()
    data class Unknown(override val cause: Throwable? = null) : DomainError(cause)
}

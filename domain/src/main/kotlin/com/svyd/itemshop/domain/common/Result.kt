package com.svyd.itemshop.domain.common

/**
 * Domain-level Result type. Avoids leaking platform exceptions into the UI
 * and keeps the success/error split explicit at use-case boundaries.
 */
sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(value))
    is DomainResult.Failure -> this
}

inline fun <T> DomainResult<T>.onSuccess(block: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) block(value)
    return this
}

inline fun <T> DomainResult<T>.onFailure(block: (DomainError) -> Unit): DomainResult<T> {
    if (this is DomainResult.Failure) block(error)
    return this
}

package com.svyd.itemshop.data.common

import com.svyd.itemshop.domain.common.DomainError
import com.svyd.itemshop.domain.common.DomainResult
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import java.io.IOException

/**
 * Translate platform exceptions into typed `DomainError`s. Used by every
 * repository so the rest of the app never sees a Ktor / IO exception.
 */
internal fun Throwable.toDomainError(): DomainError = when (this) {
    is CancellationException -> throw this
    is ClientRequestException ->
        if (response.status == HttpStatusCode.Unauthorized) DomainError.Unauthorized(this)
        else DomainError.Unknown(this)
    is ServerResponseException -> DomainError.Network(this)
    is RedirectResponseException -> DomainError.Network(this)
    is HttpRequestTimeoutException,
    is SocketTimeoutException,
    is ConnectTimeoutException,
    is IOException -> DomainError.Network(this)
    else -> DomainError.Unknown(this)
}

internal inline fun <T> runCatchingDomain(block: () -> T): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (t: Throwable) {
    if (t is CancellationException) throw t
    DomainResult.Failure(t.toDomainError())
}

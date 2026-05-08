package com.svyd.itemshop.domain.auth

import kotlinx.datetime.Instant

/**
 * Immutable user session, including the access token and its expiry.
 * Domain code never reads tokens directly — repositories and use cases pass
 * `AuthSession` references around and the data layer attaches the token to
 * outbound requests.
 */
data class AuthSession(
    val userId: String,
    val username: String,
    val accessToken: AccessToken,
)

@JvmInline
value class AccessToken(val raw: String) {
    init {
        require(raw.isNotBlank()) { "Access token must not be blank" }
    }
}

/**
 * Coarse-grained auth state that the UI shell observes to decide whether to
 * show the logged-in graph or the login screen.
 */
sealed interface AuthState {
    data object Unknown : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val session: AuthSession) : AuthState
}

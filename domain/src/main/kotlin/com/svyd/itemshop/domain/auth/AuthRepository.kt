package com.svyd.itemshop.domain.auth

import com.svyd.itemshop.domain.common.DomainResult
import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for authentication. Concrete implementations
 * back this with the platform OAuth flow (Custom Tabs on Android), token
 * exchange, and persistent token storage. Callers only know about
 * `AuthState` transitions and high-level imperative actions.
 */
interface AuthRepository {

    /** Hot stream of the current auth state. */
    val authState: Flow<AuthState>

    /**
     * Build the authorization URL the platform layer should open in a
     * browser tab to start the OAuth flow.
     */
    fun buildAuthorizationUrl(): String

    /**
     * Hand off a redirect URI received via deep link. The repository extracts
     * the authorization code and exchanges it for an access token.
     */
    suspend fun completeAuthorization(redirectUri: String): DomainResult<AuthSession>

    /** Read the latest non-Unknown state lazily (used for cold checks). */
    suspend fun currentSession(): AuthSession?

    /** Forget the session and revoke remote credentials when supported. */
    suspend fun signOut(): DomainResult<Unit>
}

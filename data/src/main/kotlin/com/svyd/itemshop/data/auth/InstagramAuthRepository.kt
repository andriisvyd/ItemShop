package com.svyd.itemshop.data.auth

import com.svyd.itemshop.data.auth.remote.InstagramAuthApi
import com.svyd.itemshop.data.common.AppCoroutineScope
import com.svyd.itemshop.data.common.runCatchingDomain
import com.svyd.itemshop.domain.auth.AccessToken
import com.svyd.itemshop.domain.auth.AuthRepository
import com.svyd.itemshop.domain.auth.AuthSession
import com.svyd.itemshop.domain.auth.AuthState
import com.svyd.itemshop.domain.auth.TokenStorage
import com.svyd.itemshop.domain.common.DomainError
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.common.map
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Default implementation that:
 *   - constructs the OAuth authorization URL,
 *   - exchanges the redirect code for an access token (and upgrades it to a
 *     long-lived token),
 *   - persists the resulting session via `TokenStorage`,
 *   - exposes a hot `authState` derived from storage.
 *
 * No knowledge of Custom Tabs, browsers, or activities lives here — the
 * "open the URL" responsibility is in the presentation layer.
 */
internal class InstagramAuthRepository(
    private val config: InstagramOAuthConfig,
    private val api: InstagramAuthApi,
    private val tokenStorage: TokenStorage,
    appScope: AppCoroutineScope,
) : AuthRepository {

    override val authState: Flow<AuthState> =
        tokenStorage.session
            .map { session -> session?.let(AuthState::Authenticated) ?: AuthState.Unauthenticated }
            .onStart<AuthState> { emit(AuthState.Unknown) }
            .stateIn(
                scope = appScope.scope,
                started = SharingStarted.Eagerly,
                initialValue = AuthState.Unknown,
            )

    override fun buildAuthorizationUrl(): String {
        check(config.isConfigured) {
            "InstagramOAuthConfig is missing client id / redirect URI; " +
                "set instagram.clientId and instagram.redirect.* in local.properties"
        }
        return URLBuilder(Url(config.authorizationEndpoint)).apply {
            parameters.append("client_id", config.clientId)
            parameters.append("redirect_uri", config.redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", config.scopes.joinToString(separator = ","))
        }.buildString()
    }

    override suspend fun completeAuthorization(redirectUri: String): DomainResult<AuthSession> {
        val code = when (val parsed = parseRedirect(redirectUri)) {
            null -> return DomainResult.Failure(DomainError.OAuthDenied("Malformed redirect URI"))
            is RedirectResult.Denied -> return DomainResult.Failure(DomainError.OAuthDenied(parsed.description))
            is RedirectResult.Code -> parsed.code
        }

        return runCatchingDomain {
            val short = api.exchangeCodeForToken(code)
            val longLived = runCatching { api.upgradeToLongLivedToken(short.accessToken) }.getOrNull()
            val effectiveToken = longLived?.accessToken ?: short.accessToken

            val user = api.fetchSelf(effectiveToken)
            AuthSession(
                userId = user.id,
                username = user.username,
                accessToken = AccessToken(effectiveToken),
            )
        }.also { result ->
            result.map { tokenStorage.save(it) }
        }
    }

    override suspend fun currentSession(): AuthSession? = tokenStorage.read()

    override suspend fun signOut(): DomainResult<Unit> = runCatchingDomain {
        tokenStorage.clear()
    }

    /** Strip the redirect URL down to either the auth code or an error. */
    private fun parseRedirect(redirectUri: String): RedirectResult? {
        val url = runCatching { Url(redirectUri) }.getOrNull() ?: return null
        val params = url.parameters
        params["error"]?.let { return RedirectResult.Denied(params["error_description"]) }
        val code = params["code"] ?: return null
        return RedirectResult.Code(code)
    }

    private sealed interface RedirectResult {
        data class Code(val code: String) : RedirectResult
        data class Denied(val description: String?) : RedirectResult
    }
}

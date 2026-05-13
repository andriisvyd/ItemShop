package com.svyd.itemshop.data.auth.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.svyd.itemshop.domain.auth.AccessToken
import com.svyd.itemshop.domain.auth.AuthSession
import com.svyd.itemshop.domain.auth.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persists the auth session in a Preferences DataStore. Domain code only
 * sees `TokenStorage`; swapping to encrypted storage or KMP-friendly
 * `multiplatform-settings` is a one-line binding change.
 */
internal class DataStoreTokenStorage(
    private val dataStore: DataStore<Preferences>,
) : TokenStorage {

    override val session: Flow<AuthSession?> = dataStore.data.map { it.toSession() }

    override suspend fun read(): AuthSession? = dataStore.data.first().toSession()

    override suspend fun save(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[Keys.UserId] = session.userId
            prefs[Keys.Username] = session.username
            prefs[Keys.AccessToken] = session.accessToken.raw
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun Preferences.toSession(): AuthSession? {
        val userId = this[Keys.UserId] ?: return null
        val username = this[Keys.Username] ?: return null
        val token = this[Keys.AccessToken] ?: return null
        return AuthSession(
            userId = userId,
            username = username,
            accessToken = AccessToken(token),
        )
    }

    private object Keys {
        val UserId = stringPreferencesKey("user_id")
        val Username = stringPreferencesKey("username")
        val AccessToken = stringPreferencesKey("access_token")
    }
}

package com.svyd.itemshop.domain.auth

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over persistent session storage. Implementations on Android use
 * DataStore; on KMP targets this can be backed by `multiplatform-settings` or
 * a Keychain wrapper. Keeps the repository ignorant of the platform.
 */
interface TokenStorage {
    val session: Flow<AuthSession?>
    suspend fun read(): AuthSession?
    suspend fun save(session: AuthSession)
    suspend fun clear()
}

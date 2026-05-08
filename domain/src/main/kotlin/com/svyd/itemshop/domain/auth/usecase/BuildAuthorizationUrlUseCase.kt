package com.svyd.itemshop.domain.auth.usecase

import com.svyd.itemshop.domain.auth.AuthRepository

/**
 * Returns the URL the platform layer should open in a browser tab to start
 * the OAuth flow. Pure delegation today, but isolating it as a use case
 * keeps the ViewModel free of `AuthRepository` and lets us insert
 * cross-cutting concerns (logging, analytics, A/B'd auth providers) later.
 */
class BuildAuthorizationUrlUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): String = authRepository.buildAuthorizationUrl()
}

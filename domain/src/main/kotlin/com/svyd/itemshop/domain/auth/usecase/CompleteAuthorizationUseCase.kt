package com.svyd.itemshop.domain.auth.usecase

import com.svyd.itemshop.domain.auth.AuthRepository
import com.svyd.itemshop.domain.auth.AuthSession
import com.svyd.itemshop.domain.common.DomainResult

/**
 * Hand off the redirect URI received via deep link to the auth repository,
 * which extracts the code and exchanges it for an access token.
 */
class CompleteAuthorizationUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(redirectUri: String): DomainResult<AuthSession> =
        authRepository.completeAuthorization(redirectUri)
}

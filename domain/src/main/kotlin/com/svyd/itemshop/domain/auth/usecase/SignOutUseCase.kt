package com.svyd.itemshop.domain.auth.usecase

import com.svyd.itemshop.domain.auth.AuthRepository
import com.svyd.itemshop.domain.common.DomainResult

class SignOutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): DomainResult<Unit> = authRepository.signOut()
}

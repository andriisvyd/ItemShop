package com.svyd.itemshop.domain.auth.usecase

import com.svyd.itemshop.domain.auth.AuthRepository
import com.svyd.itemshop.domain.auth.AuthState
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState
}

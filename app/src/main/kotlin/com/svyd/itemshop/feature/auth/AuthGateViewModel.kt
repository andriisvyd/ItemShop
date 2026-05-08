package com.svyd.itemshop.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.auth.AuthState
import com.svyd.itemshop.domain.auth.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Single source of truth for "should we show the logged-in graph?". Lives at
 * the root of the UI so login/logout transitions are reflected immediately
 * across the whole app without callbacks or nav backstack manipulation.
 */
class AuthGateViewModel(
    observeAuthState: ObserveAuthStateUseCase,
) : ViewModel() {

    val authState: StateFlow<AuthState> = observeAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.Unknown,
        )
}

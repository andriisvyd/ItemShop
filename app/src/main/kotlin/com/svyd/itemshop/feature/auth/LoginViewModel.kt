package com.svyd.itemshop.feature.auth

import androidx.lifecycle.ViewModel
import com.svyd.itemshop.domain.auth.usecase.BuildAuthorizationUrlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Drives the login screen. Note we do *not* hold a reference to
 * `AuthRepository` or to the platform launcher — instead we depend on a
 * use case for the URL and on the abstract `OAuthLauncher` for the side
 * effect of opening it. This keeps the ViewModel testable without any
 * Android primitives.
 *
 * Token exchange is intentionally not handled here. It happens in
 * `OAuthRedirectActivity` so it survives the Custom Tab returning while
 * the user might be on a different screen / process state.
 */
class LoginViewModel(
    private val buildAuthorizationUrl: BuildAuthorizationUrlUseCase,
    private val oAuthLauncher: OAuthLauncher,
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onSignInClicked() {
        if (_state.value is LoginUiState.Authorizing) return
        runCatching { buildAuthorizationUrl() }
            .onSuccess { url ->
                _state.value = LoginUiState.Authorizing()
                oAuthLauncher.launch(url)
            }
            .onFailure { t ->
                _state.update { LoginUiState.Idle(errorMessage = t.message) }
            }
    }

    fun onAuthorizationFlowAborted() {
        _state.update { LoginUiState.Idle(errorMessage = it.errorMessage) }
    }

    fun onAuthorizationFailed(message: String?) {
        _state.value = LoginUiState.Idle(errorMessage = message)
    }
}

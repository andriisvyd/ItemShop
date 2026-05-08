package com.svyd.itemshop.feature.auth

/**
 * UI state for the login screen. We keep the surface narrow on purpose;
 * everything the screen needs is one of these three values plus an
 * optional error message.
 */
sealed interface LoginUiState {
    val errorMessage: String?

    data class Idle(override val errorMessage: String? = null) : LoginUiState
    data class Authorizing(override val errorMessage: String? = null) : LoginUiState
    data class ExchangingToken(override val errorMessage: String? = null) : LoginUiState
}

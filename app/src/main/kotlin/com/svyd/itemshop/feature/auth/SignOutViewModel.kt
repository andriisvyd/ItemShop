package com.svyd.itemshop.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.itemshop.domain.auth.usecase.SignOutUseCase
import kotlinx.coroutines.launch

/**
 * Lightweight ViewModel exposing the sign-out action. Lives in the auth
 * feature so any screen that wants a "Sign out" affordance (top app bar
 * menu, settings, etc.) can hoist it without re-implementing the
 * coroutine boilerplate.
 */
class SignOutViewModel(
    private val signOut: SignOutUseCase,
) : ViewModel() {

    fun onSignOutClicked() {
        viewModelScope.launch { signOut() }
    }
}

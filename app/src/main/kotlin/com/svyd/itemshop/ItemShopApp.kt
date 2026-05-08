package com.svyd.itemshop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svyd.itemshop.domain.auth.AuthState
import com.svyd.itemshop.feature.auth.AuthGateViewModel
import com.svyd.itemshop.feature.auth.LoginScreen
import com.svyd.itemshop.navigation.MainNavHost
import org.koin.androidx.compose.koinViewModel

/**
 * Root composable. Owns the auth gate that decides whether to show the
 * login screen or the post-login navigation graph. Keeps login outside the
 * main NavHost so the navigation graph never has to special-case
 * unauthenticated state.
 */
@Composable
fun ItemShopApp() {
    val authViewModel: AuthGateViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (authState) {
            AuthState.Unknown -> SplashLoading()
            AuthState.Unauthenticated -> LoginScreen()
            is AuthState.Authenticated -> MainNavHost()
        }
    }
}

@Composable
private fun SplashLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

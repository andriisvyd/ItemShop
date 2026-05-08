package com.svyd.itemshop.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svyd.itemshop.R
import com.svyd.itemshop.domain.auth.AuthState
import org.koin.androidx.compose.koinViewModel

/**
 * Temporary post-login screen. Confirms the auth flow worked by greeting
 * the user and exposing a sign-out button. Replaced by `ProductsListScreen`
 * in the next phase.
 */
@Composable
internal fun SignedInPlaceholderScreen(
    authViewModel: AuthGateViewModel = koinViewModel(),
    signOutViewModel: SignOutViewModel = koinViewModel(),
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val username = (authState as? AuthState.Authenticated)?.session?.username.orEmpty()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Signed in as @$username",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = signOutViewModel::onSignOutClicked) {
                Text(stringResource(id = R.string.products_logout))
            }
        }
    }
}

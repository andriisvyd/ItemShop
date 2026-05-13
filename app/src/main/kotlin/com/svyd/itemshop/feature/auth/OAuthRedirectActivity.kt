package com.svyd.itemshop.feature.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.svyd.itemshop.domain.auth.usecase.CompleteAuthorizationUseCase
import com.svyd.itemshop.domain.common.DomainResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Receives the Custom Tab redirect (`itemshop://oauth/callback?code=...`),
 * fires the token exchange, and finishes itself. We launch the work on a
 * detached scope because:
 *  - this activity is `noHistory` and finishes immediately,
 *  - persisting the session via DataStore must not be cancelled if the
 *    user task-switches before completion,
 *  - the resulting `AuthState` flip is observed by `AuthGateViewModel`
 *    which lives independently of this activity.
 */
class OAuthRedirectActivity : Activity() {

    private val completeAuthorization: CompleteAuthorizationUseCase by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleRedirect(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleRedirect(intent)
        finish()
    }

    private fun handleRedirect(intent: Intent?) {
        val redirectUri = intent?.data?.toString() ?: return
        scope.launch {
            when (val result = completeAuthorization(redirectUri)) {
                is DomainResult.Success -> Unit
                is DomainResult.Failure -> {
                    // Errors propagate as a no-op for now; the AuthGate will
                    // remain on the login screen because no session was
                    // saved. Phase 3 surfaces these via a snackbar channel.
                    @Suppress("unused") val ignored = result.error
                }
            }
        }
    }
}

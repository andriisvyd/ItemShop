package com.svyd.itemshop.feature.auth

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

/**
 * Android implementation of `OAuthLauncher` that opens the authorization URL
 * in a Chrome Custom Tab. We pass `FLAG_ACTIVITY_NEW_TASK` because we may
 * launch from a non-activity context held by the ViewModel.
 */
internal class CustomTabsOAuthLauncher(
    private val context: Context,
) : OAuthLauncher {

    override fun launch(url: String) {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.launchUrl(context, url.toUri())
    }
}

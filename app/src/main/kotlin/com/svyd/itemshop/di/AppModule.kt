package com.svyd.itemshop.di

import com.svyd.itemshop.BuildConfig
import com.svyd.itemshop.data.auth.InstagramOAuthConfig
import com.svyd.itemshop.feature.auth.CustomTabsOAuthLauncher
import com.svyd.itemshop.feature.auth.OAuthLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Bindings that can only be provided by the `:app` module:
 *  - `InstagramOAuthConfig`: Android-side credentials sourced from
 *    `BuildConfig`, which is generated for the application module from
 *    `secrets.properties`. `:data` deliberately has no knowledge of
 *    `BuildConfig` so this lives here.
 *  - `OAuthLauncher`: the Android-platform implementation of the OAuth
 *    browser launch (Custom Tabs). Other ports of the app would supply a
 *    different implementation.
 *
 * Cross-cutting infra (HttpClient, AppCoroutineScope) is owned by
 * `:data/common/di/CommonDataModule.kt`. Layer-specific bindings live in
 * each layer module's own `*Module` files.
 */
val appModule = module {

    single {
        InstagramOAuthConfig(
            clientId = BuildConfig.INSTAGRAM_CLIENT_ID,
            clientSecret = BuildConfig.INSTAGRAM_CLIENT_SECRET,
            redirectUri = InstagramOAuthConfig.REDIRECT_URI,
        )
    }

    single<OAuthLauncher> { CustomTabsOAuthLauncher(androidContext()) }
}

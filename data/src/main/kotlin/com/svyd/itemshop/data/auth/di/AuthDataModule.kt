package com.svyd.itemshop.data.auth.di

import com.svyd.itemshop.data.auth.InstagramAuthRepository
import com.svyd.itemshop.data.auth.local.DataStoreTokenStorage
import com.svyd.itemshop.data.auth.local.authDataStore
import com.svyd.itemshop.data.auth.remote.InstagramAuthApi
import com.svyd.itemshop.domain.auth.AuthRepository
import com.svyd.itemshop.domain.auth.TokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * DI bindings for the auth data sources. Lives next to the impls so
 * `InstagramAuthRepository`, `InstagramAuthApi` and `DataStoreTokenStorage`
 * stay `internal` to `:data` while still being constructable from the
 * Koin graph.
 *
 * Domain-layer interfaces (`AuthRepository`, `TokenStorage`) are the only
 * symbols that escape: the rest of the app sees the abstractions and
 * never the concrete classes.
 */
val authDataModule = module {

    single<TokenStorage> { DataStoreTokenStorage(androidContext().authDataStore) }
    single { InstagramAuthApi(httpClient = get(), config = get()) }
    single<AuthRepository> {
        InstagramAuthRepository(
            config = get(),
            api = get(),
            tokenStorage = get(),
            appScope = get(),
        )
    }
}

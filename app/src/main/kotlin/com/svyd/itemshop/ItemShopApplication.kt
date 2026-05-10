package com.svyd.itemshop

import android.app.Application
import com.svyd.itemshop.data.auth.di.authDataModule
import com.svyd.itemshop.data.common.di.commonDataModule
import com.svyd.itemshop.data.posts.di.postsDataModule
import com.svyd.itemshop.data.products.di.productsDataModule
import com.svyd.itemshop.di.appModule
import com.svyd.itemshop.di.presentationModule
import com.svyd.itemshop.domain.auth.di.authDomainModule
import com.svyd.itemshop.domain.posts.di.postsDomainModule
import com.svyd.itemshop.domain.products.di.productsDomainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ItemShopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.ERROR)
            androidContext(this@ItemShopApplication)
            modules(
                // App-only bindings (Android plumbing, BuildConfig values).
                appModule,
                presentationModule,
                // Layer modules: each feature owns its own DI bindings, which
                // keeps internals encapsulated and lets features migrate
                // (or move to KMP) without touching the wiring.
                authDomainModule,
                productsDomainModule,
                postsDomainModule,
                commonDataModule(enableHttpLogging = BuildConfig.DEBUG),
                authDataModule,
                productsDataModule,
                postsDataModule,
            )
        }
    }
}

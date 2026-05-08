package com.svyd.itemshop.data.common.di

import com.svyd.itemshop.data.common.AppCoroutineScope
import com.svyd.itemshop.data.common.HttpClientFactory
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Cross-cutting infrastructure that all data-layer features rely on:
 *  - the shared Ktor `HttpClient`,
 *  - a process-wide `AppCoroutineScope` for hot flows / fire-and-forget work.
 *
 * Built as a function so callers (the app layer) can pass in environment
 * flags like `enableHttpLogging` without `:data` having to know about
 * `BuildConfig`.
 */
fun commonDataModule(enableHttpLogging: Boolean): Module = module {

    single { AppCoroutineScope(CoroutineScope(SupervisorJob() + Dispatchers.Default)) }

    single<HttpClient> { HttpClientFactory.create(enableLogging = enableHttpLogging) }
}

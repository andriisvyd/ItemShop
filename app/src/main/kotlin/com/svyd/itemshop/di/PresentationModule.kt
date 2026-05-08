package com.svyd.itemshop.di

import com.svyd.itemshop.feature.auth.AuthGateViewModel
import com.svyd.itemshop.feature.auth.LoginViewModel
import com.svyd.itemshop.feature.auth.SignOutViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Bindings for the presentation layer (ViewModels). Feature ViewModels are
 * registered here so the presentation layer is the only consumer of
 * `koinViewModel()` Composables.
 */
val presentationModule = module {

    viewModelOf(::AuthGateViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignOutViewModel)
}

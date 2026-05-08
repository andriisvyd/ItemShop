package com.svyd.itemshop.di

import org.koin.dsl.module

/**
 * Bindings for the presentation layer (ViewModels). Feature ViewModels are
 * registered here so the presentation layer is the only consumer of
 * `koinViewModel()` Composables.
 */
val presentationModule = module {
}

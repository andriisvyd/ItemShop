package com.svyd.itemshop.di

import com.svyd.itemshop.feature.auth.AuthGateViewModel
import com.svyd.itemshop.feature.auth.LoginViewModel
import com.svyd.itemshop.feature.auth.SignOutViewModel
import com.svyd.itemshop.feature.posts.PostsListViewModel
import com.svyd.itemshop.feature.products.details.ProductDetailsViewModel
import com.svyd.itemshop.feature.products.edit.EditProductViewModel
import com.svyd.itemshop.feature.products.list.ProductsListViewModel
import org.koin.core.module.dsl.viewModel
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

    viewModelOf(::ProductsListViewModel)
    viewModelOf(::PostsListViewModel)

    // EditProductViewModel and ProductDetailsViewModel take their target
    // id from the navigation route, so they're bound with explicit
    // parameter resolvers instead of `viewModelOf`.
    viewModel { (id: String) ->
        EditProductViewModel(
            id = id,
            getInstagramPost = get(),
            buildDraftFromPost = get(),
            saveProduct = get(),
        )
    }

    viewModel { (id: String) ->
        ProductDetailsViewModel(id = id, getProduct = get())
    }
}

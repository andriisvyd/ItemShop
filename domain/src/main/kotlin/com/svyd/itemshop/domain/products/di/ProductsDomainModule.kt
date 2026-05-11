package com.svyd.itemshop.domain.products.di

import com.svyd.itemshop.domain.products.usecase.BuildProductDraftFromPostUseCase
import com.svyd.itemshop.domain.products.usecase.DeleteProductUseCase
import com.svyd.itemshop.domain.products.usecase.GetProductUseCase
import com.svyd.itemshop.domain.products.usecase.MarkReadyToShipUseCase
import com.svyd.itemshop.domain.products.usecase.MarkShippedUseCase
import com.svyd.itemshop.domain.products.usecase.ObserveProductsUseCase
import com.svyd.itemshop.domain.products.usecase.RevertToAvailableUseCase
import com.svyd.itemshop.domain.products.usecase.RevertToReadyToShipUseCase
import com.svyd.itemshop.domain.products.usecase.SaveProductUseCase
import com.svyd.itemshop.domain.products.usecase.SyncProductsFromInstagramUseCase
import kotlinx.datetime.Clock
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Built as a function (rather than a top-level `val`) because the sync
 * use case needs a fallback title string sourced from the app's string
 * resources. Mirrors the shape of `commonDataModule(enableHttpLogging)`.
 */
fun productsDomainModule(fallbackProductTitle: String): Module = module {
    // Single instance is fine — Clock.System is stateless. Any use case that
    // needs the wall clock should depend on this binding rather than calling
    // Clock.System directly, so tests can override with a fake clock.
    single<Clock> { Clock.System }

    factoryOf(::ObserveProductsUseCase)
    factoryOf(::GetProductUseCase)
    factoryOf(::SaveProductUseCase)
    factoryOf(::DeleteProductUseCase)
    factoryOf(::BuildProductDraftFromPostUseCase)

    // Status transitions.
    factoryOf(::MarkReadyToShipUseCase)
    factoryOf(::MarkShippedUseCase)
    factoryOf(::RevertToReadyToShipUseCase)
    factoryOf(::RevertToAvailableUseCase)

    factory {
        SyncProductsFromInstagramUseCase(
            instagramPostsRepository = get(),
            productsRepository = get(),
            clock = get(),
            fallbackTitle = fallbackProductTitle,
        )
    }
}

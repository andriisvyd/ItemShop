package com.svyd.itemshop.domain.products.di

import com.svyd.itemshop.domain.products.usecase.BuildProductDraftFromPostUseCase
import com.svyd.itemshop.domain.products.usecase.DeleteProductUseCase
import com.svyd.itemshop.domain.products.usecase.GetProductUseCase
import com.svyd.itemshop.domain.products.usecase.ObserveProductsUseCase
import com.svyd.itemshop.domain.products.usecase.SaveProductUseCase
import kotlinx.datetime.Clock
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val productsDomainModule = module {
    // Single instance is fine — Clock.System is stateless. Any use case that
    // needs the wall clock should depend on this binding rather than calling
    // Clock.System directly, so tests can override with a fake clock.
    single<Clock> { Clock.System }

    factoryOf(::ObserveProductsUseCase)
    factoryOf(::GetProductUseCase)
    factoryOf(::SaveProductUseCase)
    factoryOf(::DeleteProductUseCase)
    factoryOf(::BuildProductDraftFromPostUseCase)
}

package com.svyd.itemshop.domain.auth.di

import com.svyd.itemshop.domain.auth.usecase.BuildAuthorizationUrlUseCase
import com.svyd.itemshop.domain.auth.usecase.CompleteAuthorizationUseCase
import com.svyd.itemshop.domain.auth.usecase.ObserveAuthStateUseCase
import com.svyd.itemshop.domain.auth.usecase.SignOutUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * DI bindings for auth-related domain use cases. Lives next to the use
 * cases themselves so this module is self-contained: when auth eventually
 * becomes a `:feature:auth` module, the bindings move with it.
 */
val authDomainModule = module {
    factoryOf(::BuildAuthorizationUrlUseCase)
    factoryOf(::CompleteAuthorizationUseCase)
    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::SignOutUseCase)
}

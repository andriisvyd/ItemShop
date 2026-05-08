package com.svyd.itemshop.di

import org.koin.dsl.module

/**
 * Bindings for domain use cases. Domain itself stays free of DI framework
 * dependencies — wiring of use cases happens here, in the app layer.
 */
val domainModule = module {
}

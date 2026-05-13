package com.svyd.itemshop.di

import org.koin.dsl.module

/**
 * Bindings for the data layer (repository implementations, network client,
 * database, persistence). Domain-layer interfaces are bound to concrete
 * implementations here so the rest of the graph can depend on the abstractions.
 */
val dataModule = module {
}

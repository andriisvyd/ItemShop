package com.svyd.itemshop.domain.posts.di

import com.svyd.itemshop.domain.posts.usecase.GetInstagramPostUseCase
import com.svyd.itemshop.domain.posts.usecase.LoadInstagramPostsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val postsDomainModule = module {
    factoryOf(::LoadInstagramPostsUseCase)
    factoryOf(::GetInstagramPostUseCase)
}

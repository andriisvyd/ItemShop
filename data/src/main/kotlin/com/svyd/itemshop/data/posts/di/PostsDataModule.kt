package com.svyd.itemshop.data.posts.di

import com.svyd.itemshop.data.posts.KtorInstagramPostsRepository
import com.svyd.itemshop.data.posts.remote.InstagramGraphApi
import com.svyd.itemshop.domain.posts.InstagramPostsRepository
import org.koin.dsl.module

val postsDataModule = module {
    single { InstagramGraphApi(httpClient = get(), config = get()) }
    single<InstagramPostsRepository> {
        KtorInstagramPostsRepository(api = get(), tokenStorage = get())
    }
}

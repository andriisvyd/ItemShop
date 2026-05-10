package com.svyd.itemshop.data.products.di

import androidx.room.Room
import com.svyd.itemshop.data.products.RoomProductsRepository
import com.svyd.itemshop.data.products.local.ItemShopDatabase
import com.svyd.itemshop.data.products.local.ProductDao
import com.svyd.itemshop.domain.products.ProductsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val productsDataModule = module {

    single<ItemShopDatabase> {
        Room.databaseBuilder(
            context = androidContext(),
            klass = ItemShopDatabase::class.java,
            name = ItemShopDatabase.NAME,
        )
            // Phase 3 ships with version 1 only; replace with explicit
            // migrations once the schema evolves.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single<ProductDao> { get<ItemShopDatabase>().productDao() }

    single<ProductsRepository> { RoomProductsRepository(dao = get()) }
}

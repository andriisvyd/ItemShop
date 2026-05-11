package com.svyd.itemshop.data.products.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class],
    version = 2,
    exportSchema = true,
)
internal abstract class ItemShopDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        const val NAME = "item_shop.db"
    }
}

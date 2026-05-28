package com.naltech.kasiva.pos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.naltech.kasiva.pos.data.local.dao.InventoryDao
import com.naltech.kasiva.pos.data.local.dao.TransactionDao
import com.naltech.kasiva.pos.data.local.entities.CategoryEntity
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionItemEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "kasiva_pos_db"
    }
}

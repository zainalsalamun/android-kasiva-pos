package com.naltech.kasiva.pos

import android.app.Application
import androidx.room.Room
import com.naltech.kasiva.pos.data.local.AppDatabase
import com.naltech.kasiva.pos.data.local.LocalDataSeeder
import com.naltech.kasiva.pos.data.repository.InventoryRepository
import com.naltech.kasiva.pos.data.repository.TransactionRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class KasivaApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(database.inventoryDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            LocalDataSeeder.seedIfEmpty(database)
        }
        com.naltech.kasiva.pos.util.SyncManager.startPeriodicSync(this)
    }
}

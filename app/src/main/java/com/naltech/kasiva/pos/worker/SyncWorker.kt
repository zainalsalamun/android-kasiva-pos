package com.naltech.kasiva.pos.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naltech.kasiva.pos.KasivaApp
import com.naltech.kasiva.pos.data.remote.SyncApiService
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as KasivaApp
        val inventoryRepo = app.inventoryRepository
        val transactionRepo = app.transactionRepository

        // In a real app, this would come from a proper DI setup or remote config
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.kasiva-pos.com/") // Placeholder URL
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val apiService = retrofit.create(SyncApiService::class.java)

        return try {
            val unsyncedProducts = inventoryRepo.getAllProducts().first().filter { !it.isSynced }
            if (unsyncedProducts.isNotEmpty()) {
                // apiService.syncProducts(unsyncedProducts)
                Log.d("SyncWorker", "Syncing ${unsyncedProducts.size} products...")
                // After successful sync, update local DB. For now, just logging.
            }

            val unsyncedTransactions = transactionRepo.getAllTransactions().first().filter { !it.isSynced }
            if (unsyncedTransactions.isNotEmpty()) {
                // apiService.syncTransactions(unsyncedTransactions)
                Log.d("SyncWorker", "Syncing ${unsyncedTransactions.size} transactions...")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}

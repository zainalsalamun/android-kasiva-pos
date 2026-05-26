package com.naltech.kasiva.pos.util

import android.content.Context
import androidx.work.*
import com.naltech.kasiva.pos.worker.SyncWorker
import java.util.concurrent.TimeUnit

object SyncManager {
    private const val SYNC_WORK_NAME = "KasivaSyncWork"

    fun startPeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

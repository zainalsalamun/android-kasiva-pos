package com.naltech.kasiva.pos.data.local.dao

import androidx.room.*
import com.naltech.kasiva.pos.data.local.entities.TransactionEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Transaction
    suspend fun insertCompleteTransaction(transaction: TransactionEntity, items: List<TransactionItemEntity>) {
        val id = insertTransaction(transaction)
        val itemsWithId = items.map { it.copy(transactionId = id) }
        insertTransactionItems(itemsWithId)
    }
}

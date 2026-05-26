package com.naltech.kasiva.pos.data.repository

import com.naltech.kasiva.pos.data.local.dao.TransactionDao
import com.naltech.kasiva.pos.data.local.entities.TransactionEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun saveTransaction(transaction: TransactionEntity, items: List<TransactionItemEntity>) =
        transactionDao.insertCompleteTransaction(transaction, items)
}

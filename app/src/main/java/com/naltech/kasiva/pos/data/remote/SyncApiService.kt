package com.naltech.kasiva.pos.data.remote

import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApiService {
    @POST("sync/products")
    suspend fun syncProducts(@Body products: List<ProductEntity>): Response<Unit>

    @POST("sync/transactions")
    suspend fun syncTransactions(@Body transactions: List<TransactionEntity>): Response<Unit>
}

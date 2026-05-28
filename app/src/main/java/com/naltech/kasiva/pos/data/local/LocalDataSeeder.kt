package com.naltech.kasiva.pos.data.local

import com.naltech.kasiva.pos.data.local.entities.CategoryEntity
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionItemEntity

object LocalDataSeeder {
    suspend fun seedIfEmpty(database: AppDatabase) {
        val inventoryDao = database.inventoryDao()
        val transactionDao = database.transactionDao()

        if (inventoryDao.getCategoryCount() == 0) {
            inventoryDao.insertCategories(defaultCategories)
        }

        if (inventoryDao.getProductCount() == 0) {
            inventoryDao.insertProducts(defaultProducts)
        }

        if (transactionDao.getTransactionCount() == 0) {
            defaultTransactions.forEach { (transaction, items) ->
                transactionDao.insertCompleteTransaction(transaction, items)
            }
        }
    }

    private val defaultCategories = listOf(
        CategoryEntity(id = 1, name = "Minuman"),
        CategoryEntity(id = 2, name = "Makanan"),
        CategoryEntity(id = 3, name = "Sembako"),
        CategoryEntity(id = 4, name = "Snack"),
        CategoryEntity(id = 5, name = "Kebersihan"),
        CategoryEntity(id = 6, name = "Lainnya")
    )

    private val defaultProducts = listOf(
        ProductEntity(id = 1, categoryId = 1, name = "Aqua 600ml", sku = "8992745756012", buyPrice = 3000.0, price = 4000.0, stock = 120),
        ProductEntity(id = 2, categoryId = 2, name = "Indomie Goreng", sku = "089686010012", buyPrice = 2800.0, price = 3500.0, stock = 80),
        ProductEntity(id = 3, categoryId = 2, name = "Indomie Soto", sku = "089686010029", buyPrice = 2800.0, price = 3500.0, stock = 75),
        ProductEntity(id = 4, categoryId = 1, name = "Kopi Good Day", sku = "8991002101122", buyPrice = 1800.0, price = 2500.0, stock = 60),
        ProductEntity(id = 5, categoryId = 2, name = "Roti Coklat", sku = "899999900005", buyPrice = 3500.0, price = 5000.0, stock = 40),
        ProductEntity(id = 6, categoryId = 3, name = "Gula Pasir 1kg", sku = "899999900006", buyPrice = 11000.0, price = 13000.0, stock = 35),
        ProductEntity(id = 7, categoryId = 1, name = "Teh Pucuk 350ml", sku = "8996001600269", buyPrice = 2500.0, price = 3500.0, stock = 90),
        ProductEntity(id = 8, categoryId = 1, name = "Susu Ultra 250ml", sku = "8998009010565", buyPrice = 4500.0, price = 6000.0, stock = 45),
        ProductEntity(id = 9, categoryId = 4, name = "Chitato 68g", sku = "899999900009", buyPrice = 5500.0, price = 7000.0, stock = 30)
    )

    private val now = System.currentTimeMillis()

    private val defaultTransactions = listOf(
        TransactionEntity(timestamp = now - 10 * 60 * 1000, subtotal = 125000.0, tax = 0.0, discount = 0.0, totalAmount = 125000.0, paymentMethod = "Tunai", isSynced = true) to
            listOf(TransactionItemEntity(transactionId = 0, productId = 1, quantity = 10, priceAtTransaction = 4000.0)),
        TransactionEntity(timestamp = now - 25 * 60 * 1000, subtotal = 98000.0, tax = 0.0, discount = 0.0, totalAmount = 98000.0, paymentMethod = "QRIS", isSynced = true) to
            listOf(TransactionItemEntity(transactionId = 0, productId = 2, quantity = 8, priceAtTransaction = 3500.0)),
        TransactionEntity(timestamp = now - 40 * 60 * 1000, subtotal = 250000.0, tax = 0.0, discount = 0.0, totalAmount = 250000.0, paymentMethod = "Tunai", isSynced = false) to
            listOf(TransactionItemEntity(transactionId = 0, productId = 6, quantity = 5, priceAtTransaction = 13000.0))
    )
}

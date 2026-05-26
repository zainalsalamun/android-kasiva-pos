package com.naltech.kasiva.pos.data.repository

import com.naltech.kasiva.pos.data.local.dao.InventoryDao
import com.naltech.kasiva.pos.data.local.entities.CategoryEntity
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {
    fun getAllProducts(): Flow<List<ProductEntity>> = inventoryDao.getAllProducts()

    suspend fun getProductById(id: Long): ProductEntity? = inventoryDao.getProductById(id)

    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>> = 
        inventoryDao.getProductsByCategory(categoryId)

    suspend fun saveProduct(product: ProductEntity) {
        if (product.id == 0L) {
            inventoryDao.insertProduct(product)
        } else {
            inventoryDao.updateProduct(product)
        }
    }

    suspend fun deleteProduct(product: ProductEntity) = inventoryDao.deleteProduct(product)

    fun getAllCategories(): Flow<List<CategoryEntity>> = inventoryDao.getAllCategories()

    suspend fun saveCategory(category: CategoryEntity) = inventoryDao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = inventoryDao.deleteCategory(category)
}

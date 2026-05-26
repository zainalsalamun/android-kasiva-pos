package com.naltech.kasiva.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val sku: String,
    val buyPrice: Double = 0.0,
    val price: Double,
    val stock: Int,
    val unit: String = "Pcs",
    val description: String = "",
    val rackLocation: String = "",
    val imageUrl: String? = null,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

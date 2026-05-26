package com.naltech.kasiva.pos.feature.transaction

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = listOf("Semua", "Minuman", "Makanan", "Sembako", "Lainnya"),
    val selectedCategory: String = "Semua",
    val cartItems: List<CartItemState> = emptyList(),
    val isLoading: Boolean = false
) {
    val subtotal: Double get() = cartItems.sumOf { it.price * it.quantity }
    val tax: Double get() = 0.0 // mockup shows 0%
    val total: Double get() = subtotal + tax
}

@Immutable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val imageUrl: String? = null
)

@Immutable
data class CartItemState(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int
)

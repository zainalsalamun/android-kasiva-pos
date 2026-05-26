package com.naltech.kasiva.pos.feature.transaction

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        val dummyProducts = listOf(
            Product("1", "Aqua 600ml", 4.0, "Minuman"),
            Product("2", "Indomie Goreng", 3.0, "Makanan"),
            Product("3", "Indomie Soto", 3.0, "Makanan"),
            Product("4", "Kopi Good Day", 2.0, "Minuman"),
            Product("5", "Roti Coklat", 5.0, "Makanan"),
            Product("6", "Gula Pasir 1kg", 13.0, "Sembako"),
            Product("7", "Teh Pucuk 350ml", 3.5, "Minuman"),
            Product("8", "Susu Ultra 250ml", 6.0, "Minuman"),
            Product("9", "Chitato 68g", 7.0, "Makanan")
        )
        _uiState.update { 
            it.copy(
                products = dummyProducts,
                categories = listOf("Semua", "Minuman", "Makanan", "Sembako", "Lainnya"),
                selectedCategory = "Semua"
            ) 
        }
    }

    fun onProductClick(product: Product) {
        _uiState.update { state ->
            val existingItem = state.cartItems.find { it.productId == product.id }
            val newCartItems = if (existingItem != null) {
                state.cartItems.map {
                    if (it.productId == product.id) it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                state.cartItems + CartItemState(product.id, product.name, product.price, 1)
            }
            state.copy(cartItems = newCartItems)
        }
    }

    fun onQuantityUpdate(productId: String, delta: Int) {
        _uiState.update { state ->
            val newCartItems = state.cartItems.mapNotNull {
                if (it.productId == productId) {
                    val newQty = it.quantity + delta
                    if (newQty > 0) it.copy(quantity = newQty) else null
                } else it
            }
            state.copy(cartItems = newCartItems)
        }
    }

    fun onCategorySelect(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList()) }
    }
}

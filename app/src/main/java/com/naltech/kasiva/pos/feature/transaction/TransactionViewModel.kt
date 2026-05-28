package com.naltech.kasiva.pos.feature.transaction

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
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
        _uiState.update { 
            it.copy(
                categories = listOf("Semua", "Minuman", "Makanan", "Sembako", "Lainnya"),
                selectedCategory = "Semua"
            ) 
        }
    }

    fun setLocalProducts(products: List<ProductEntity>) {
        _uiState.update { state ->
            state.copy(
                products = products.map {
                    Product(
                        id = it.id.toString(),
                        name = it.name,
                        price = it.price,
                        category = categoryName(it.categoryId),
                        imageUrl = it.imageUrl
                    )
                },
                categories = listOf("Semua", "Minuman", "Makanan", "Sembako", "Snack", "Kebersihan", "Lainnya")
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

    private fun categoryName(categoryId: Long): String = when (categoryId) {
        1L -> "Minuman"
        2L -> "Makanan"
        3L -> "Sembako"
        4L -> "Snack"
        5L -> "Kebersihan"
        else -> "Lainnya"
    }
}

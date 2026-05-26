package com.naltech.kasiva.pos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionItemEntity
import com.naltech.kasiva.pos.data.repository.InventoryRepository
import com.naltech.kasiva.pos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class POSViewModel(
    private val inventoryRepository: InventoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val products: StateFlow<List<ProductEntity>> = combine(
        inventoryRepository.getAllProducts(),
        _searchQuery
    ) { products, query ->
        if (query.isBlank()) products
        else products.filter { it.name.contains(query, ignoreCase = true) || it.sku.contains(query) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAmount: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: ProductEntity) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.product.id == product.id }
            if (existingItem != null) {
                currentItems.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                currentItems + CartItem(product, 1)
            }
        }
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = products.value.find { it.sku == barcode }
            if (product != null) {
                addToCart(product)
            }
        }
    }

    fun updateQuantity(product: ProductEntity, delta: Int) {
        _cartItems.update { currentItems ->
            currentItems.mapNotNull {
                if (it.product.id == product.id) {
                    val newQuantity = it.quantity + delta
                    if (newQuantity > 0) it.copy(quantity = newQuantity) else null
                } else it
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun completeTransaction(paymentMethod: String) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            val transaction = TransactionEntity(
                totalAmount = items.sumOf { it.totalPrice },
                paymentMethod = paymentMethod
            )
            val transactionItems = items.map {
                TransactionItemEntity(
                    transactionId = 0, // Set by DAO
                    productId = it.product.id,
                    quantity = it.quantity,
                    priceAtTransaction = it.product.price
                )
            }
            transactionRepository.saveTransaction(transaction, transactionItems)
            clearCart()
        }
    }

    class Factory(
        private val inventoryRepository: InventoryRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return POSViewModel(inventoryRepository, transactionRepository) as T
        }
    }
}

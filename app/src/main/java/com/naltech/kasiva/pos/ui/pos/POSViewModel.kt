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

    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount = _discount.asStateFlow()

    val categories = inventoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = combine(
        inventoryRepository.getAllProducts(),
        _searchQuery,
        _selectedCategory
    ) { products, query, categoryId ->
        var filtered = products
        if (query.isNotBlank()) {
            filtered = filtered.filter { it.name.contains(query, ignoreCase = true) || it.sku.contains(query) }
        }
        if (categoryId != null) {
            filtered = filtered.filter { it.categoryId == categoryId }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subtotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val taxRate = 0.10 // 10% tax as per design

    val taxAmount: StateFlow<Double> = subtotal.map { it * taxRate }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val finalTotal: StateFlow<Double> = combine(subtotal, taxAmount, _discount) { sub, tax, disc ->
        (sub + tax - disc).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChange(categoryId: Long?) {
        _selectedCategory.value = categoryId
    }

    fun onDiscountChange(amount: String) {
        _discount.value = amount.toDoubleOrNull() ?: 0.0
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
                subtotal = subtotal.value,
                tax = taxAmount.value,
                discount = _discount.value,
                totalAmount = finalTotal.value,
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

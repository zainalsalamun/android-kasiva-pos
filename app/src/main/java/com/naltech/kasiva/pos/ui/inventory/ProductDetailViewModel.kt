package com.naltech.kasiva.pos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naltech.kasiva.pos.data.local.entities.CategoryEntity
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import com.naltech.kasiva.pos.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val repository: InventoryRepository,
    private val productId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (productId != null) {
            viewModelScope.launch {
                val product = repository.getProductById(productId)
                if (product != null) {
                    _uiState.update { 
                        it.copy(
                            id = product.id,
                            name = product.name,
                            sku = product.sku,
                            price = product.price.toString(),
                            stock = product.stock.toString(),
                            categoryId = product.categoryId,
                            imageUrl = product.imageUrl
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onSkuChange(sku: String) = _uiState.update { it.copy(sku = sku) }
    fun onPriceChange(price: String) = _uiState.update { it.copy(price = price) }
    fun onStockChange(stock: String) = _uiState.update { it.copy(stock = stock) }
    fun onCategoryChange(categoryId: Long) = _uiState.update { it.copy(categoryId = categoryId) }

    fun saveProduct() {
        val state = _uiState.value
        val product = ProductEntity(
            id = state.id,
            name = state.name,
            sku = state.sku,
            price = state.price.toDoubleOrNull() ?: 0.0,
            stock = state.stock.toIntOrNull() ?: 0,
            categoryId = state.categoryId,
            imageUrl = state.imageUrl
        )
        viewModelScope.launch {
            repository.saveProduct(product)
        }
    }

    class Factory(
        private val repository: InventoryRepository,
        private val productId: Long?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductDetailViewModel(repository, productId) as T
        }
    }
}

data class ProductDetailUiState(
    val id: Long = 0,
    val name: String = "",
    val sku: String = "",
    val price: String = "",
    val stock: String = "",
    val categoryId: Long = 0,
    val imageUrl: String? = null
)

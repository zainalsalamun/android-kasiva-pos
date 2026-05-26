package com.naltech.kasiva.pos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import com.naltech.kasiva.pos.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .map { categoryEntities ->
            listOf("Semua") + categoryEntities.map { it.name }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            listOf("Semua", "Minuman", "Makanan", "Sembako", "Snack", "Kebersihan", "Lainnya")
        )

    val products: StateFlow<List<ProductEntity>> = combine(
        repository.getAllProducts(),
        repository.getAllCategories(),
        _searchQuery,
        _selectedCategory
    ) { products, categories, query, category ->
        var filtered = products
        
        if (query.isNotBlank()) {
            filtered = filtered.filter { it.name.contains(query, ignoreCase = true) || it.sku.contains(query) }
        }
        
        if (category != "Semua") {
            val categoryId = categories.firstOrNull { it.name == category }?.id
            filtered = filtered.filter { it.categoryId == categoryId }
        }
        
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(repository) as T
        }
    }
}

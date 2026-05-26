package com.naltech.kasiva.pos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import com.naltech.kasiva.pos.data.local.entities.TransactionEntity
import com.naltech.kasiva.pos.data.repository.InventoryRepository
import com.naltech.kasiva.pos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*

class DashboardViewModel(
    private val inventoryRepository: InventoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val totalSales: StateFlow<Double> = transactionRepository.getAllTransactions()
        .map { transactions -> transactions.sumOf { it.totalAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val transactionCount: StateFlow<Int> = transactionRepository.getAllTransactions()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockProducts: StateFlow<List<ProductEntity>> = inventoryRepository.getAllProducts()
        .map { products -> products.filter { it.stock < 10 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(
        private val inventoryRepository: InventoryRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(inventoryRepository, transactionRepository) as T
        }
    }
}

package com.naltech.kasiva.pos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naltech.kasiva.pos.data.repository.InventoryRepository
import com.naltech.kasiva.pos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val inventoryRepository: InventoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeLocalData()
    }

    private fun observeLocalData() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
        viewModelScope.launch {
            combine(
                inventoryRepository.getAllProducts(),
                transactionRepository.getAllTransactions()
            ) { products, transactions ->
                val recentTransactions = transactions.take(5).mapIndexed { index, transaction ->
                    TransactionItem(
                        invoice = "INV-${transaction.id.toString().padStart(9, '0')}",
                        time = dateFormat.format(Date(transaction.timestamp)),
                        cashier = "Kasir ${(index % 3) + 1}",
                        amount = transaction.totalAmount,
                        method = transaction.paymentMethod,
                        status = if (transaction.isSynced) "Tersync" else "Belum Sync"
                    )
                }
                DashboardUiState(
                    totalSales = transactions.sumOf { it.totalAmount },
                    transactionCount = transactions.size,
                    productCount = products.size,
                    lowStockCount = products.count { it.stock <= 30 },
                    recentTransactions = recentTransactions,
                    topProducts = products.take(5).map {
                        TopProduct(it.name, it.stock.coerceAtLeast(1), it.price * it.stock.coerceAtLeast(1))
                    },
                    salesHistory = buildSalesHistory(transactions.map { it.totalAmount })
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun buildSalesHistory(amounts: List<Double>): List<SalesPoint> {
        val days = listOf("Sab", "Min", "Sen", "Sel", "Rab", "Kam", "Hari ini")
        if (amounts.isEmpty()) return days.map { SalesPoint(it, 0.0) }
        return days.mapIndexed { index, day ->
            val amount = amounts.getOrNull(index % amounts.size) ?: 0.0
            SalesPoint(day, amount / 100000.0)
        }
    }

    class Factory(
        private val inventoryRepository: InventoryRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(inventoryRepository, transactionRepository) as T
        }
    }
}

data class DashboardUiState(
    val totalSales: Double = 0.0,
    val transactionCount: Int = 0,
    val productCount: Int = 0,
    val lowStockCount: Int = 0,
    val recentTransactions: List<TransactionItem> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
    val salesHistory: List<SalesPoint> = emptyList()
)

data class TransactionItem(
    val invoice: String,
    val time: String,
    val cashier: String,
    val amount: Double,
    val method: String,
    val status: String
)

data class TopProduct(
    val name: String,
    val soldCount: Int,
    val totalRevenue: Double
)

data class SalesPoint(
    val day: String,
    val value: Double
)

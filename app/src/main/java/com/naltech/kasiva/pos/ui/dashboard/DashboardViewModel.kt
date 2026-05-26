package com.naltech.kasiva.pos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naltech.kasiva.pos.data.repository.InventoryRepository
import com.naltech.kasiva.pos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*

class DashboardViewModel(
    private val inventoryRepository: InventoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val recentTransactions = listOf(
            TransactionItem("INV-250501-001", "01 Mei 2025 10:45", "Kasir 1", 125000.0, "Tunai", "Tersync"),
            TransactionItem("INV-250501-002", "01 Mei 2025 10:30", "Kasir 1", 98000.0, "QRIS", "Tersync"),
            TransactionItem("INV-250501-003", "01 Mei 2025 10:15", "Kasir 2", 250000.0, "Tunai", "Belum Sync"),
            TransactionItem("INV-250501-004", "01 Mei 2025 10:05", "Kasir 1", 75000.0, "Tunai", "Tersync"),
            TransactionItem("INV-250501-005", "01 Mei 2025 09:50", "Kasir 2", 45000.0, "QRIS", "Tersync")
        )

        val topProducts = listOf(
            TopProduct("Aqua 600ml", 320, 1280000.0),
            TopProduct("Indomie Goreng", 280, 980000.0),
            TopProduct("Kopi Good Day", 210, 525000.0),
            TopProduct("Teh Pucuk 350ml", 180, 540000.0),
            TopProduct("Roti Coklat", 150, 750000.0)
        )

        val salesHistory = listOf(
            SalesPoint("Sab", 3.0),
            SalesPoint("Min", 5.0),
            SalesPoint("Sen", 4.5),
            SalesPoint("Sel", 6.2),
            SalesPoint("Rab", 5.8),
            SalesPoint("Kam", 4.0),
            SalesPoint("Jum", 7.5)
        )

        _uiState.update { 
            it.copy(
                totalSales = 12450000.0,
                transactionCount = 156,
                productCount = 1234,
                lowStockCount = 23,
                recentTransactions = recentTransactions,
                topProducts = topProducts,
                salesHistory = salesHistory
            )
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

package com.naltech.kasiva.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.naltech.kasiva.pos.ui.dashboard.DashboardScreen
import com.naltech.kasiva.pos.ui.dashboard.DashboardViewModel
import com.naltech.kasiva.pos.ui.inventory.*
import com.naltech.kasiva.pos.ui.navigation.NavKey
import com.naltech.kasiva.pos.ui.pos.CheckoutScreen
import com.naltech.kasiva.pos.ui.pos.POSScreen
import com.naltech.kasiva.pos.ui.pos.POSViewModel
import com.naltech.kasiva.pos.ui.theme.AndroidkasivaposTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidkasivaposTheme {
                val app = LocalContext.current.applicationContext as KasivaApp
                val backStack = remember { mutableStateListOf<NavKey>(NavKey.POS) }

                val windowAdaptiveInfo = currentWindowAdaptiveInfo()
                val directive = remember(windowAdaptiveInfo) {
                    calculatePaneScaffoldDirective(windowAdaptiveInfo)
                        .copy(horizontalPartitionSpacerSize = 0.dp)
                }
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

                Scaffold(
                    bottomBar = {
                        val currentKey = backStack.lastOrNull()
                        if (currentKey is NavKey.POS || currentKey is NavKey.Inventory || currentKey is NavKey.Dashboard) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentKey is NavKey.POS,
                                    onClick = {
                                        backStack.clear()
                                        backStack.add(NavKey.POS)
                                    },
                                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
                                    label = { Text("POS") }
                                )
                                NavigationBarItem(
                                    selected = currentKey is NavKey.Dashboard,
                                    onClick = {
                                        backStack.clear()
                                        backStack.add(NavKey.Dashboard)
                                    },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                    label = { Text("Dashboard") }
                                )
                                NavigationBarItem(
                                    selected = currentKey is NavKey.Inventory,
                                    onClick = {
                                        backStack.clear()
                                        backStack.add(NavKey.Inventory)
                                    },
                                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                                    label = { Text("Inventory") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        // Shared POSViewModel scoped to the Activity
                        val sharedPosViewModel: POSViewModel = viewModel(
                            viewModelStoreOwner = LocalActivity.current as ComponentActivity,
                            factory = POSViewModel.Factory(app.inventoryRepository, app.transactionRepository)
                        )

                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            sceneStrategy = listDetailStrategy,
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            entryProvider = entryProvider {
                                entry<NavKey.Dashboard> {
                                    val dashboardViewModel: DashboardViewModel = viewModel(
                                        factory = DashboardViewModel.Factory(app.inventoryRepository, app.transactionRepository)
                                    )
                                    DashboardScreen(viewModel = dashboardViewModel)
                                }
                                entry<NavKey.POS> {
                                    POSScreen(
                                        viewModel = sharedPosViewModel,
                                        onCheckout = { backStack.add(NavKey.Checkout) }
                                    )
                                }
                                entry<NavKey.Checkout> {
                                    CheckoutScreen(
                                        viewModel = sharedPosViewModel,
                                        onBack = { backStack.removeLastOrNull() },
                                        onComplete = {
                                            backStack.clear()
                                            backStack.add(NavKey.POS)
                                        }
                                    )
                                }
                                entry<NavKey.Inventory>(
                                    metadata = ListDetailSceneStrategy.listPane()
                                ) {
                                    val viewModel: InventoryViewModel = viewModel(
                                        factory = InventoryViewModel.Factory(app.inventoryRepository)
                                    )
                                    val products by viewModel.products.collectAsState()
                                    ProductListScreen(
                                        products = products,
                                        onProductClick = { backStack.add(NavKey.ProductDetail(it)) },
                                        onAddProduct = { backStack.add(NavKey.ProductDetail(null)) },
                                        onDeleteProduct = { viewModel.deleteProduct(it) },
                                        onManageCategories = { backStack.add(NavKey.Categories) }
                                    )
                                }
                                entry<NavKey.Categories>(
                                    metadata = ListDetailSceneStrategy.detailPane()
                                ) {
                                    val viewModel: CategoryViewModel = viewModel(
                                        factory = CategoryViewModel.Factory(app.inventoryRepository)
                                    )
                                    CategoryListScreen(
                                        viewModel = viewModel,
                                        onBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                entry<NavKey.ProductDetail>(
                                    metadata = ListDetailSceneStrategy.detailPane()
                                ) { key ->
                                    val viewModel: ProductDetailViewModel = viewModel(
                                        factory = ProductDetailViewModel.Factory(
                                            app.inventoryRepository,
                                            key.productId
                                        )
                                    )
                                    ProductDetailScreen(
                                        viewModel = viewModel,
                                        onBack = { backStack.removeLastOrNull() }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

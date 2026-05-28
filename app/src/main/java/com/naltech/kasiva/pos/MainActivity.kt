package com.naltech.kasiva.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.naltech.kasiva.pos.feature.auth.LoginScreen
import com.naltech.kasiva.pos.feature.splash.SplashScreen
import com.naltech.kasiva.pos.feature.transaction.TransactionScreen
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
                val backStack = remember { mutableStateListOf<NavKey>(NavKey.Splash) }

                val windowAdaptiveInfo = currentWindowAdaptiveInfo()
                val directive = remember(windowAdaptiveInfo) {
                    calculatePaneScaffoldDirective(windowAdaptiveInfo)
                        .copy(horizontalPartitionSpacerSize = 0.dp)
                }
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
                val widthClass = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass
                val showAppChrome = backStack.lastOrNull().let {
                    it is NavKey.POS || it is NavKey.Inventory || it is NavKey.Dashboard || it is NavKey.Transactions
                }
                val useSideNav = showAppChrome && widthClass != WindowWidthSizeClass.COMPACT

                Scaffold(
                    bottomBar = {
                        val currentKey = backStack.lastOrNull()
                        if (showAppChrome && !useSideNav) {
                            NavigationBar(containerColor = Color.White) {
                                AppNavItem("Dashboard", Icons.Default.Dashboard, currentKey is NavKey.Dashboard) {
                                    backStack.replaceWith(NavKey.Dashboard)
                                }
                                AppNavItem("Produk", Icons.Default.Inventory, currentKey is NavKey.Inventory) {
                                    backStack.replaceWith(NavKey.Inventory)
                                }
                                AppNavItem("Transaksi", Icons.Default.PointOfSale, currentKey is NavKey.POS) {
                                    backStack.replaceWith(NavKey.POS)
                                }
                                AppNavItem("Riwayat", Icons.Default.History, currentKey is NavKey.Transactions) {
                                    backStack.replaceWith(NavKey.Transactions)
                                }
                                AppNavItem("Pengaturan", Icons.Default.Settings, false) {}
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        val currentKey = backStack.lastOrNull()
                        if (useSideNav) {
                            KasivaSideNav(
                                currentKey = currentKey,
                                onNavigate = { key -> backStack.replaceWith(key) }
                            )
                        }
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                entry<NavKey.Splash> {
                                    SplashScreen(
                                        onTimeout = {
                                            backStack.clear()
                                            backStack.add(NavKey.Login)
                                        }
                                    )
                                }
                                entry<NavKey.Login> {
                                    LoginScreen(
                                        onLoginSuccess = {
                                            backStack.clear()
                                            backStack.add(NavKey.POS)
                                        }
                                    )
                                }
                                entry<NavKey.Dashboard> {
                                    val dashboardViewModel: DashboardViewModel = viewModel(
                                        factory = DashboardViewModel.Factory(app.inventoryRepository, app.transactionRepository)
                                    )
                                    DashboardScreen(viewModel = dashboardViewModel)
                                }
                                entry<NavKey.POS> {
                                    TransactionScreen()
                                }
                                entry<NavKey.Transactions> {
                                    TransactionScreen(historyMode = true)
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
                                    ProductListScreen(
                                        viewModel = viewModel,
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
}

private fun MutableList<NavKey>.replaceWith(key: NavKey) {
    clear()
    add(key)
}

@Composable
private fun RowScope.AppNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) }
    )
}

@Composable
private fun KasivaSideNav(
    currentKey: NavKey?,
    onNavigate: (NavKey) -> Unit
) {
    Column(
        modifier = Modifier
            .width(232.dp)
            .fillMaxHeight()
            .background(Brush.verticalGradient(listOf(Color(0xFF02569B), Color(0xFF023E73))))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp, bottom = 28.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF02569B))
            }
            Spacer(Modifier.width(10.dp))
            Text("Kasiva POS", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        SideNavItem("Dashboard", Icons.Default.Dashboard, currentKey is NavKey.Dashboard) { onNavigate(NavKey.Dashboard) }
        SideNavItem("Produk", Icons.Default.Inventory, currentKey is NavKey.Inventory || currentKey is NavKey.ProductDetail) { onNavigate(NavKey.Inventory) }
        SideNavItem("Transaksi", Icons.Default.PointOfSale, currentKey is NavKey.POS) { onNavigate(NavKey.POS) }
        SideNavItem("Riwayat", Icons.Default.History, currentKey is NavKey.Transactions) { onNavigate(NavKey.Transactions) }
        SideNavItem("Laporan", Icons.Default.Assessment, false) {}
        SideNavItem("Pengaturan", Icons.Default.Settings, false) {}

        Spacer(Modifier.weight(1f))
        Surface(
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFFFE2C6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BZ", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Bang Zai", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Owner", color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("Versi 1.0.0", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SideNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFF0B6BEF),
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = Color.White,
            unselectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedTextColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(vertical = 3.dp)
    )
}

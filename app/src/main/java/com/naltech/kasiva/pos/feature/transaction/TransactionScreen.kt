package com.naltech.kasiva.pos.feature.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naltech.kasiva.pos.KasivaApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BlueAccent = Color(0xFF02569B)
private val LightGrayBackground = Color(0xFFF8F9FA)
private val BorderColor = Color(0xFFE9ECEF)

@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = viewModel(),
    historyMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val app = LocalContext.current.applicationContext as KasivaApp
    val localProducts by app.inventoryRepository.getAllProducts().collectAsState(initial = emptyList())

    LaunchedEffect(localProducts) {
        viewModel.setLocalProducts(localProducts)
    }

    if (historyMode) {
        HistoryContent()
    } else {
        TransactionContent(
            uiState = uiState,
            onProductClick = viewModel::onProductClick,
            onCategorySelect = viewModel::onCategorySelect,
            onQuantityUpdate = viewModel::onQuantityUpdate,
            onClearCart = viewModel::clearCart
        )
    }
}

@Composable
private fun TransactionContent(
    uiState: TransactionUiState,
    onProductClick: (Product) -> Unit,
    onCategorySelect: (String) -> Unit,
    onQuantityUpdate: (String, Int) -> Unit,
    onClearCart: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val isCompact = maxWidth < 840.dp
        val filteredProducts = uiState.products.filter {
            uiState.selectedCategory == "Semua" || it.category == uiState.selectedCategory
        }

        if (isCompact) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {}) { Icon(Icons.Default.Menu, contentDescription = null) }
                        SearchBar(modifier = Modifier.weight(1f), showFilter = false)
                        IconButton(onClick = {}) { Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = BlueAccent) }
                    }
                }
                item {
                    CategorySection(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelect = onCategorySelect
                    )
                }
                item {
                    ProductGridSection(
                        products = filteredProducts,
                        columns = 2,
                        modifier = Modifier.height(430.dp),
                        onProductClick = onProductClick
                    )
                }
                item {
                    CartPanel(
                        uiState = uiState,
                        onQuantityUpdate = onQuantityUpdate,
                        onClearCart = onClearCart,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1.6f)
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF334155))
                        SearchBar(modifier = Modifier.weight(1f))
                        ScanBarcodeAction(modifier = Modifier.width(180.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CategorySection(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelect = onCategorySelect
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ProductGridSection(
                        products = filteredProducts,
                        columns = 4,
                        modifier = Modifier.weight(1f),
                        onProductClick = onProductClick
                    )
                }

                CartPanel(
                    uiState = uiState,
                    onQuantityUpdate = onQuantityUpdate,
                    onClearCart = onClearCart,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    showFilter: Boolean = true
) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Cari produk / barcode", color = Color.Gray) },
        modifier = modifier
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = if (showFilter) ({ Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Gray) }) else null,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF1F3F5),
            focusedContainerColor = Color(0xFFF1F3F5),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = BlueAccent
        )
    )
}

@Composable
fun CategorySection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 12.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(
                onClick = { onCategorySelect(category) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) BlueAccent else Color(0xFFF1F3F5),
                contentColor = if (isSelected) Color.White else Color.DarkGray,
                modifier = Modifier.height(36.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGridSection(
    products: List<Product>,
    columns: Int,
    modifier: Modifier = Modifier,
    onProductClick: (Product) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(products) { product ->
            ProductCard(product = product, onClick = { onProductClick(product) })
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F3F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Fastfood,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.LightGray
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = String.format(Locale.US, "Rp %.0f", product.price),
                color = BlueAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CartHeader(onClearCart: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Keranjang",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onClearCart) {
            Text("Bersihkan", color = Color.Red, fontSize = 14.sp)
        }
    }
}

@Composable
fun ScanBarcodeAction() {
    ScanBarcodeAction(modifier = Modifier.fillMaxWidth())
}

@Composable
fun ScanBarcodeAction(modifier: Modifier = Modifier) {
    Surface(
        onClick = { },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = border(width = 1.dp, color = BlueAccent.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = BlueAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan Barcode", color = BlueAccent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CartPanel(
    uiState: TransactionUiState,
    onQuantityUpdate: (String, Int) -> Unit,
    onClearCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(LightGrayBackground)
            .border(width = 1.dp, color = BorderColor)
            .padding(16.dp)
    ) {
        CartHeader(onClearCart = onClearCart)
        Spacer(modifier = Modifier.height(12.dp))
        CartItemList(
            cartItems = uiState.cartItems,
            onQuantityUpdate = onQuantityUpdate
        )
        Spacer(modifier = Modifier.height(12.dp))
        SummarySection(
            subtotal = uiState.subtotal,
            tax = uiState.tax,
            total = uiState.total
        )
    }
}

private fun border(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun CartItemList(
    cartItems: List<CartItemState>,
    onQuantityUpdate: (String, Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        cartItems.forEach { item ->
            CartItemRow(item = item, onQuantityUpdate = onQuantityUpdate)
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemState,
    onQuantityUpdate: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE9ECEF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.LightGray)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                text = String.format(Locale.US, "Rp %.0f", item.price),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { onQuantityUpdate(item.productId, -1) },
                modifier = Modifier.size(24.dp).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            
            Text(text = item.quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            IconButton(
                onClick = { onQuantityUpdate(item.productId, 1) },
                modifier = Modifier.size(24.dp).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = String.format(Locale.US, "Rp %.0f", item.price * item.quantity),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.widthIn(min = 60.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun SummarySection(
    subtotal: Double,
    tax: Double,
    total: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryRow(label = "Subtotal", value = subtotal)
        SummaryRow(label = "Diskon", value = 0.0)
        SummaryRow(label = "Pajak (0%)", value = tax)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                text = String.format(Locale.US, "Rp %.0f", total),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
        ) {
            Text(text = "Checkout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = String.format(Locale.US, "Rp %.0f", value), fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun HistoryContent() {
    val app = LocalContext.current.applicationContext as KasivaApp
    val transactions by app.transactionRepository.getAllTransactions().collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")) }
    val items = transactions.mapIndexed { index, transaction ->
        HistoryItem(
            invoice = "INV-${transaction.id.toString().padStart(9, '0')}",
            date = dateFormat.format(Date(transaction.timestamp)),
            cashier = "Kasir ${(index % 3) + 1}",
            customer = "Pelanggan Umum",
            total = transaction.totalAmount,
            status = if (transaction.isSynced) "Tersync" else "Belum Sync"
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
    ) {
        val isCompact = maxWidth < 720.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(if (isCompact) 14.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Riwayat Transaksi", fontSize = if (isCompact) 20.sp else 24.sp, fontWeight = FontWeight.Bold)
                        if (!isCompact) Text("Kelola dan pantau semua transaksi penjualan Anda", color = Color.Gray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {}, contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(18.dp))
                            if (!isCompact) {
                                Spacer(Modifier.width(8.dp))
                                Text("Export")
                            }
                        }
                        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                            if (!isCompact) {
                                Spacer(Modifier.width(8.dp))
                                Text("Filter")
                            }
                        }
                    }
                }
            }
            item { SearchBar(modifier = Modifier.fillMaxWidth()) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(listOf("Semua", "Tersync", "Belum Sync", "Dibatalkan")) { status ->
                        Surface(
                            onClick = {},
                            shape = RoundedCornerShape(20.dp),
                            color = if (status == "Semua") BlueAccent else Color(0xFFF1F5F9),
                            contentColor = if (status == "Semua") Color.White else Color(0xFF334155)
                        ) {
                            Text(status, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (isCompact) {
                items(items) { item -> HistoryCard(item) }
            } else {
                item { HistoryTable(items) }
            }
        }
    }
}

@Composable
private fun HistoryTable(items: List<HistoryItem>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                Text("Invoice", Modifier.weight(1.4f), fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Tanggal", Modifier.weight(1.4f), fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Kasir", Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Pelanggan", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Total", Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Status", Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.End)
            }
            items.forEach {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(it.invoice, Modifier.weight(1.4f), fontWeight = FontWeight.SemiBold)
                    Text(it.date, Modifier.weight(1.4f), color = Color.Gray)
                    Text(it.cashier, Modifier.weight(0.9f))
                    Text(it.customer, Modifier.weight(1.2f), color = Color.Gray)
                    Text(String.format(Locale.US, "Rp %.0f", it.total), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    StatusBadge(it.status, Modifier.weight(0.9f))
                }
                HorizontalDivider(color = BorderColor)
            }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val color = when (item.status) {
                "Tersync" -> Color(0xFF22C55E)
                "Belum Sync" -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(color), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.invoice, fontWeight = FontWeight.Bold)
                Text("${item.date} • ${item.cashier}", color = Color.Gray, fontSize = 12.sp)
                Text(item.customer, color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format(Locale.US, "Rp %.0f", item.total), fontWeight = FontWeight.Bold)
                StatusBadge(item.status)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = when (status) {
        "Tersync" -> Color(0xFF22C55E)
        "Belum Sync" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
            Text(status, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

private data class HistoryItem(
    val invoice: String,
    val date: String,
    val cashier: String,
    val customer: String,
    val total: Double,
    val status: String
)

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun TransactionScreenPreview() {
    val dummyProducts = listOf(
        Product("1", "Aqua 600ml", 4.0, "Minuman"),
        Product("2", "Indomie Goreng", 3.0, "Makanan"),
        Product("3", "Indomie Soto", 3.0, "Makanan"),
        Product("4", "Kopi Good Day", 2.0, "Minuman"),
        Product("5", "Roti Coklat", 5.0, "Makanan"),
        Product("6", "Gula Pasir 1kg", 13.0, "Sembako")
    )
    val dummyCart = listOf(
        CartItemState("1", "Aqua 600ml", 4.0, 2),
        CartItemState("2", "Indomie Goreng", 3.0, 1)
    )
    TransactionContent(
        uiState = TransactionUiState(
            products = dummyProducts,
            cartItems = dummyCart,
            categories = listOf("Semua", "Minuman", "Makanan", "Sembako", "Lainnya"),
            selectedCategory = "Semua"
        ),
        onProductClick = {},
        onCategorySelect = {},
        onQuantityUpdate = { _, _ -> },
        onClearCart = {}
    )
}

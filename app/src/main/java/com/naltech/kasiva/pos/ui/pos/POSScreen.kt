package com.naltech.kasiva.pos.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import com.naltech.kasiva.pos.util.BarcodeScannerDialog
import com.naltech.kasiva.pos.util.formatCurrency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    viewModel: POSViewModel,
    onCheckout: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategory.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val taxAmount by viewModel.taxAmount.collectAsState()
    val discount by viewModel.discount.collectAsState()
    val finalTotal by viewModel.finalTotal.collectAsState()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isTablet = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    var showScanner by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            POSTopBar(
                searchQuery = searchQuery,
                onSearchChange = viewModel::onSearchQueryChange,
                onScanClick = { showScanner = true }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (isTablet) {
            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Product Section
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .padding(horizontal = 24.dp)
                ) {
                    CategorySection(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelect = viewModel::onCategoryChange
                    )
                    
                    ProductGrid(
                        products = products,
                        onProductAdd = viewModel::addToCart
                    )

                    PaginationSection()
                }

                // Cart Section
                CartPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.White)
                        .padding(24.dp),
                    cartItems = cartItems,
                    subtotal = subtotal,
                    taxAmount = taxAmount,
                    discount = discount,
                    finalTotal = finalTotal,
                    onUpdateQuantity = viewModel::updateQuantity,
                    onRemoveItem = { viewModel.updateQuantity(it.product, -it.quantity) },
                    onClearCart = viewModel::clearCart,
                    onDiscountChange = viewModel::onDiscountChange,
                    onCheckout = onCheckout
                )
            }
        } else {
            // Mobile Layout
            Box(modifier = Modifier.padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    CategorySection(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelect = viewModel::onCategoryChange
                    )
                    ProductGrid(
                        products = products,
                        onProductAdd = viewModel::addToCart,
                        columns = 2
                    )
                }
                
                // Mobile Floating Cart Button or Summary
                if (cartItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { /* TODO: Show Bottom Sheet */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Keranjang (${cartItems.size})")
                            Spacer(Modifier.weight(1f))
                            Text(finalTotal.formatCurrency(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showScanner) {
            BarcodeScannerDialog(
                onBarcodeDetected = { barcode ->
                    viewModel.scanBarcode(barcode)
                    showScanner = false
                },
                onDismiss = { showScanner = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onScanClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { /* Open Drawer */ }) {
                Icon(Icons.Default.Menu, contentDescription = null)
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari produk / barcode") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF025EB9)
                )
            )

            OutlinedButton(
                onClick = onScanClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Scan Barcode")
            }
        }
    }
}

@Composable
fun CategorySection(
    categories: List<com.naltech.kasiva.pos.data.local.entities.CategoryEntity>,
    selectedCategoryId: Long?,
    onCategorySelect: (Long?) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            CategoryChip(
                label = "Semua",
                selected = selectedCategoryId == null,
                onClick = { onCategorySelect(null) }
            )
        }
        items(categories) { category ->
            CategoryChip(
                label = category.name,
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelect(category.id) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) Color(0xFF025EB9) else Color.White,
        border = if (!selected) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            color = if (selected) Color.White else Color(0xFF64748B),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun ProductGrid(
    products: List<ProductEntity>,
    onProductAdd: (ProductEntity) -> Unit,
    columns: Int = 5
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(products) { product ->
            ProductCard(product, onProductAdd)
        }
        
        item {
            AddProductPlaceholder()
        }
    }
}

@Composable
fun AddProductPlaceholder() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddBox, contentDescription = null, tint = Color(0xFF94A3B8))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Tambah Produk",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
            Text(
                "F2",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onProductAdd: (ProductEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Placeholder Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(40.dp))
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = product.price.formatCurrency(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF025EB9),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Stok: ${product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stock > 10) Color(0xFF64748B) else Color(0xFFEF4444)
                )
                
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onProductAdd(product) },
                    shape = CircleShape,
                    color = Color(0xFF025EB9)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaginationSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("1 - 9 dari 120 produk", color = Color(0xFF64748B), fontSize = 14.sp)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {}) { Icon(Icons.Default.ChevronLeft, contentDescription = null) }
            TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF025EB9), contentColor = Color.White)) { Text("1") }
            TextButton(onClick = {}) { Text("2") }
            TextButton(onClick = {}) { Text("3") }
            Text("...")
            TextButton(onClick = {}) { Text("14") }
            IconButton(onClick = {}) { Icon(Icons.Default.ChevronRight, contentDescription = null) }
        }
    }
}

@Composable
fun CartPanel(
    modifier: Modifier = Modifier,
    cartItems: List<CartItem>,
    subtotal: Double,
    taxAmount: Double,
    discount: Double,
    finalTotal: Double,
    onUpdateQuantity: (ProductEntity, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onDiscountChange: (String) -> Unit,
    onCheckout: () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keranjang (${cartItems.size})",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = onClearCart) {
                Text("Hapus Semua", color = Color(0xFFEF4444))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cartItems) { item ->
                CartItemRow(item, onUpdateQuantity, onRemoveItem)
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Summary Box
        SummaryBox(
            subtotal = subtotal,
            taxAmount = taxAmount,
            discount = discount,
            finalTotal = finalTotal,
            onDiscountChange = onDiscountChange,
            onCheckout = onCheckout
        )
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onUpdateQuantity: (ProductEntity, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Placeholder Image
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(24.dp))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = { onRemoveItem(item) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF94A3B8))
                }
            }
            
            Text(item.product.price.formatCurrency(), color = Color(0xFF64748B), fontSize = 14.sp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuantityStepper(
                    quantity = item.quantity,
                    onIncrease = { onUpdateQuantity(item.product, 1) },
                    onDecrease = { onUpdateQuantity(item.product, -1) }
                )
                
                Text(item.totalPrice.formatCurrency(), fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }
        }
    }
}

@Composable
fun QuantityStepper(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier.widthIn(min = 32.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SummaryBox(
    subtotal: Double,
    taxAmount: Double,
    discount: Double,
    finalTotal: Double,
    onDiscountChange: (String) -> Unit,
    onCheckout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryRow("Subtotal", subtotal.formatCurrency())
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Diskon", color = Color(0xFF64748B))
            OutlinedTextField(
                value = if (discount == 0.0) "" else discount.toString(),
                onValueChange = onDiscountChange,
                modifier = Modifier.width(100.dp),
                placeholder = { Text("0") },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
        }
        
        SummaryRow("Pajak (10%)", taxAmount.formatCurrency())
        
        HorizontalDivider(color = Color(0xFFF1F5F9))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOTAL", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                text = finalTotal.formatCurrency(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF025EB9)
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        Button(
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF025EB9))
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Checkout (F3)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF64748B))
        Text(value, fontWeight = FontWeight.Medium)
    }
}

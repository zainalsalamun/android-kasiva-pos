package com.naltech.kasiva.pos.ui.pos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naltech.kasiva.pos.util.BarcodeScannerDialog
import com.naltech.kasiva.pos.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    viewModel: POSViewModel,
    onCheckout: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    
    var showScanner by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("POS Checkout") },
                actions = {
                    IconButton(onClick = { showScanner = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = totalAmount.formatCurrency(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier.height(56.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Checkout")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding)) {
            // Product Selection (Left)
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    label = { Text("Search Products") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(8.dp))
                
                LazyColumn {
                    items(products) { product ->
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = { Text("${product.price.formatCurrency()} | SKU: ${product.sku}") },
                            modifier = Modifier.clickable { viewModel.addToCart(product) }
                        )
                    }
                }
            }
            
            VerticalDivider()
            
            Column(modifier = Modifier.weight(0.8f).padding(8.dp)) {
                Text("Cart (${cartItems.size})", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onIncrease = { viewModel.updateQuantity(item.product, 1) },
                            onDecrease = { viewModel.updateQuantity(item.product, -1) }
                        )
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

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = item.totalPrice.formatCurrency(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.product.price.formatCurrency()} x ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

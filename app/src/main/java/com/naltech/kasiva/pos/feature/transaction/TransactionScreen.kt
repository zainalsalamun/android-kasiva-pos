package com.naltech.kasiva.pos.feature.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

private val BlueAccent = Color(0xFF02569B)
private val LightGrayBackground = Color(0xFFF8F9FA)
private val BorderColor = Color(0xFFE9ECEF)

@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TransactionContent(
        uiState = uiState,
        onProductClick = viewModel::onProductClick,
        onCategorySelect = viewModel::onCategorySelect,
        onQuantityUpdate = viewModel::onQuantityUpdate,
        onClearCart = viewModel::clearCart
    )
}

@Composable
private fun TransactionContent(
    uiState: TransactionUiState,
    onProductClick: (Product) -> Unit,
    onCategorySelect: (String) -> Unit,
    onQuantityUpdate: (String, Int) -> Unit,
    onClearCart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Left Panel: Products
        Column(
            modifier = Modifier
                .weight(1.6f)
                .fillMaxHeight()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Semua Produk",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SearchBar()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CategorySection(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelect = onCategorySelect
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            ProductGridSection(
                products = uiState.products.filter { 
                    uiState.selectedCategory == "All" || it.category == uiState.selectedCategory 
                },
                onProductClick = onProductClick
            )
        }

        // Right Panel: Cart
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(LightGrayBackground)
                .border(width = 1.dp, color = BorderColor)
                .padding(20.dp)
        ) {
            CartHeader(onClearCart = onClearCart)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ScanBarcodeAction()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CartItemList(
                cartItems = uiState.cartItems,
                onQuantityUpdate = onQuantityUpdate
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            SummarySection(
                subtotal = uiState.subtotal,
                tax = uiState.tax,
                total = uiState.total
            )
        }
    }
}

@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Cari produk / barcode", color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Gray) },
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
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
    onProductClick: (Product) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
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
                text = String.format(Locale.US, "Rp %.0f", product.price * 1000), // Adjusted for mockup style
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
    Surface(
        onClick = { },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = border(width = 1.dp, color = BlueAccent.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
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

private fun border(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun CartItemList(
    cartItems: List<CartItemState>,
    onQuantityUpdate: (String, Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(cartItems) { item ->
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
                text = String.format(Locale.US, "Rp %.0f", item.price * 1000),
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
            text = String.format(Locale.US, "Rp %.0f", item.price * item.quantity * 1000),
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
                text = String.format(Locale.US, "Rp %.0f", total * 1000),
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
        Text(text = String.format(Locale.US, "Rp %.0f", value * 1000), fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

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

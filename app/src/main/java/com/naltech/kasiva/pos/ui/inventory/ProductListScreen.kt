package com.naltech.kasiva.pos.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naltech.kasiva.pos.data.local.entities.ProductEntity
import java.util.Locale

private val BlueAccent = Color(0xFF02569B)
private val BackgroundColor = Color(0xFFF8FAFC)
private val SuccessGreen = Color(0xFF22C55E)
private val BorderColor = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: InventoryViewModel,
    onProductClick: (Long) -> Unit,
    onAddProduct: () -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onManageCategories: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                BoxWithConstraints {
                    val isCompact = maxWidth < 600.dp
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Produk",
                                    fontSize = if (isCompact) 20.sp else 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                if (!isCompact) {
                                    Text(
                                        text = "Kelola semua produk toko Anda",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Button(
                                onClick = onAddProduct,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                                contentPadding = PaddingValues(horizontal = if (isCompact) 12.dp else 16.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                if (!isCompact) {
                                    Spacer(Modifier.width(8.dp))
                                    Text("Tambah Produk", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                placeholder = { Text("Cari produk / barcode / nama", color = Color.Gray, maxLines = 1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF1F5F9),
                                    focusedContainerColor = Color(0xFFF1F5F9),
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = BlueAccent
                                )
                            )

                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BlueAccent.copy(alpha = 0.3f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueAccent),
                                contentPadding = PaddingValues(horizontal = if (isCompact) 14.dp else 16.dp)
                            ) {
                                Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                                if (!isCompact) {
                                    Spacer(Modifier.width(8.dp))
                                    Text("Scan Barcode", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderColor),
                                color = Color.White,
                                onClick = onManageCategories
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 24.dp)
                ) {
                    // Use a unique name for items to avoid confusion if necessary, 
                    // but standard import from foundation.lazy works for LazyRow
                    this.items(items = categories) { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            onClick = { viewModel.onCategoryChange(category) },
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) BlueAccent else Color(0xFFF1F5F9),
                            contentColor = if (isSelected) Color.White else Color.DarkGray,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                                Text(category, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products) { product ->
                    ProductGridCard(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }
                
                item {
                    QuickAddCard()
                }
            }
            
            // Footer Pagination
            PaginationFooter(totalProducts = products.size)
        }
    }
}

@Composable
fun ProductGridCard(
    product: ProductEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.LightGray)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = String.format(Locale.US, "Rp %.0f", product.price),
                color = BlueAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Stok: ${product.stock}",
                    fontSize = 12.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium
                )
                
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = BlueAccent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAddCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor),
        onClick = {}
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp).align(Alignment.BottomEnd))
            }
            Spacer(Modifier.height(8.dp))
            Text("Tambah Cepat", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PaginationFooter(totalProducts: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menampilkan 1 - $totalProducts dari $totalProducts produk",
                fontSize = 13.sp,
                color = Color.Gray
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null) }
                
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = BlueAccent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("1", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Text("2", fontSize = 13.sp, modifier = Modifier.padding(horizontal = 8.dp))
                Text("3", fontSize = 13.sp, modifier = Modifier.padding(horizontal = 8.dp))
                Text("...", color = Color.Gray)
                Text("12", fontSize = 13.sp, modifier = Modifier.padding(horizontal = 8.dp))
                
                IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun ProductListPreview() {
    // Component Preview
}

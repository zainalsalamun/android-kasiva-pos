package com.naltech.kasiva.pos.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import com.naltech.kasiva.pos.data.local.entities.CategoryEntity
import com.naltech.kasiva.pos.util.BarcodeScannerDialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    
    var showScanner by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.id == 0L) "Tambah Produk" else "Edit Produk",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (uiState.id == 0L) "Lengkapi informasi produk baru" else "Perbarui informasi produk",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { showScanner = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Scan Barcode")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            viewModel.saveProduct()
                            onBack()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan Produk")
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (showScanner) {
            BarcodeScannerDialog(
                onBarcodeDetected = { barcode ->
                    viewModel.onSkuChange(barcode)
                    showScanner = false
                },
                onDismiss = { showScanner = false }
            )
        }

        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
            TwoColumnLayout(padding, uiState, categories, viewModel)
        } else {
            SingleColumnLayout(padding, uiState, categories, viewModel)
        }
    }
}

@Composable
fun SingleColumnLayout(
    padding: PaddingValues,
    uiState: ProductDetailUiState,
    categories: List<CategoryEntity>,
    viewModel: ProductDetailViewModel
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ProductInfoSection(uiState, categories, viewModel)
        }
        item {
            PriceSection(uiState, viewModel)
        }
        item {
            StockSection(uiState, viewModel)
        }
        item {
            AdditionalInfoSection(uiState, viewModel)
        }
        item {
            PhotoSection()
        }
    }
}

@Composable
fun TwoColumnLayout(
    padding: PaddingValues,
    uiState: ProductDetailUiState,
    categories: List<CategoryEntity>,
    viewModel: ProductDetailViewModel
) {
    Row(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProductInfoSection(uiState, categories, viewModel)
            StockSection(uiState, viewModel)
            AdditionalInfoSection(uiState, viewModel)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PriceSection(uiState, viewModel)
            PhotoSection()
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductInfoSection(
    uiState: ProductDetailUiState,
    categories: List<CategoryEntity>,
    viewModel: ProductDetailViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == uiState.categoryId }

    SectionCard(title = "Informasi Produk") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FormField(label = "Nama Produk", required = true) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = { Text("Masukkan nama produk") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            FormField(label = "Barcode") {
                OutlinedTextField(
                    value = uiState.sku,
                    onValueChange = viewModel::onSkuChange,
                    placeholder = { Text("Masukkan / Scan barcode") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    }
                )
            }

            FormField(label = "Kategori", required = true) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Pilih kategori produk",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.onCategoryChange(category.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriceSection(
    uiState: ProductDetailUiState,
    viewModel: ProductDetailViewModel
) {
    val buyPrice = uiState.buyPrice.toDoubleOrNull() ?: 0.0
    val sellPrice = uiState.price.toDoubleOrNull() ?: 0.0
    val profit = sellPrice - buyPrice

    SectionCard(title = "Harga") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                FormField(label = "Harga Beli") {
                    OutlinedTextField(
                        value = uiState.buyPrice,
                        onValueChange = viewModel::onBuyPriceChange,
                        prefix = { Text("Rp ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                FormField(label = "Harga Jual", required = true) {
                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = viewModel::onPriceChange,
                        prefix = { Text("Rp ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        FormField(label = "Keuntungan") {
            OutlinedTextField(
                value = String.format(Locale.getDefault(), "Rp %,.0f", profit),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSection(
    uiState: ProductDetailUiState,
    viewModel: ProductDetailViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val units = listOf("Pcs", "Box", "Kg", "Liter", "Pack")

    SectionCard(title = "Stok") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                FormField(label = "Jumlah Stok", required = true) {
                    OutlinedTextField(
                        value = uiState.stock,
                        onValueChange = viewModel::onStockChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                FormField(label = "Satuan") {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.unit,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        viewModel.onUnitChange(unit)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdditionalInfoSection(
    uiState: ProductDetailUiState,
    viewModel: ProductDetailViewModel
) {
    SectionCard(title = "Informasi Tambahan") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FormField(label = "Deskripsi (Opsional)") {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = { Text("Masukkan deskripsi produk") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            FormField(label = "Lokasi Rak (Opsional)") {
                OutlinedTextField(
                    value = uiState.rackLocation,
                    onValueChange = viewModel::onRackLocationChange,
                    placeholder = { Text("Contoh: Rak A1-01") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun PhotoSection() {
    SectionCard(title = "Foto Produk") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                .clickable { /* TODO: Upload Photo */ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF94A3B8)
                )
                Spacer(Modifier.height(8.dp))
                Text("Tambah Foto", fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                Text("JPEG/PNG, Maks. 2MB", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    required: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF334155)
            )
            if (required) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Red
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

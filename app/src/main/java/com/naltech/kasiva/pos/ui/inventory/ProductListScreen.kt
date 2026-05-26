package com.naltech.kasiva.pos.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.naltech.kasiva.pos.data.local.entities.ProductEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    products: List<ProductEntity>,
    onProductClick: (Long) -> Unit,
    onAddProduct: () -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onManageCategories: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
                actions = {
                    IconButton(onClick = onManageCategories) {
                        Icon(Icons.Default.Category, contentDescription = "Categories")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(products) { product ->
                ListItem(
                    headlineContent = { Text(product.name) },
                    supportingContent = { Text("SKU: ${product.sku} | Stock: ${product.stock}") },
                    trailingContent = {
                        IconButton(onClick = { onDeleteProduct(product) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    modifier = Modifier.clickable { onProductClick(product.id) }
                )
            }
        }
    }
}

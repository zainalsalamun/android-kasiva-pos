package com.naltech.kasiva.pos.ui.pos

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.naltech.kasiva.pos.hardware.BluetoothPrinterService
import com.naltech.kasiva.pos.util.formatCurrency
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: POSViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val printerService = remember { BluetoothPrinterService(context) }
    
    val cartItems by viewModel.cartItems.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val paymentMethods = listOf("Cash", "Debit Card", "Credit Card", "E-Wallet")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Summary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Items Summary", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems) { item ->
                    ListItem(
                        headlineContent = { Text(item.product.name) },
                        supportingContent = { Text("${item.quantity} x ${item.product.price.formatCurrency()}") },
                        trailingContent = { Text(item.totalPrice.formatCurrency()) }
                    )
                }
            }
            
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Amount", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = totalAmount.formatCurrency(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(24.dp))
            Text("Select Payment Method", style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paymentMethods.forEach { method ->
                    FilterChip(
                        selected = selectedPaymentMethod == method,
                        onClick = { selectedPaymentMethod = method },
                        label = { Text(method) }
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = {
                    viewModel.completeTransaction(selectedPaymentMethod)
                    showSuccessDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Complete Purchase")
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Transaction Successful") },
                text = { Text("The transaction has been completed and saved locally.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSuccessDialog = false
                        onComplete()
                    }) {
                        Text("Finish")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        scope.launch {
                            val receipt = StringBuilder()
                            receipt.append("KASIVA POS\n")
                            receipt.append("----------------\n")
                            cartItems.forEach { item ->
                                receipt.append("${item.product.name} x${item.quantity}\n")
                                receipt.append("   ${item.totalPrice.formatCurrency()}\n")
                            }
                            receipt.append("----------------\n")
                            receipt.append("Total: ${totalAmount.formatCurrency()}\n")
                            receipt.append("Payment: $selectedPaymentMethod\n")
                            receipt.append("\nThank you!\n")
                            
                            val mockDeviceAddress = "00:11:22:33:44:55"
                            if (printerService.connectToDevice(mockDeviceAddress)) {
                                printerService.printReceipt(receipt.toString())
                                printerService.disconnect()
                            } else {
                                Log.d("Checkout", "Printer not found, simulating print:\n$receipt")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Print Receipt")
                    }
                }
            )
        }
    }
}

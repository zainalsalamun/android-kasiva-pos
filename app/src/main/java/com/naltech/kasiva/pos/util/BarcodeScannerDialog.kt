package com.naltech.kasiva.pos.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BarcodeScannerDialog(
    onBarcodeDetected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BarcodeScannerScreen(
            onBarcodeDetected = onBarcodeDetected,
            onDismiss = onDismiss,
            onManualInput = { /* TODO: Show manual input dialog */ },
            onGalleryClick = { /* TODO: Open gallery */ }
        )
    }
}

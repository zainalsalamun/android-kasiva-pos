package com.naltech.kasiva.pos.util

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onDismiss: () -> Unit,
    onManualInput: () -> Unit = {},
    onGalleryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                                onBarcodeDetected(barcode)
                            })
                        }

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay & Controls
        ScannerOverlay(
            isFlashOn = isFlashOn,
            onFlashToggle = {
                isFlashOn = !isFlashOn
                camera?.cameraControl?.enableTorch(isFlashOn)
            },
            onClose = onDismiss,
            onManualInput = onManualInput,
            onGalleryClick = onGalleryClick
        )
    }
}

@Composable
fun ScannerOverlay(
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit,
    onClose: () -> Unit,
    onManualInput: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line_progress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Semi-transparent background with cutout
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val scanAreaSize = canvasWidth * 0.7f
            val left = (canvasWidth - scanAreaSize) / 2
            val top = (canvasHeight - scanAreaSize) / 2.5f // Positioned slightly above center
            
            val scanRect = Rect(left, top, left + scanAreaSize, top + scanAreaSize)
            
            // Draw overlay background
            with(drawContext.canvas.nativeCanvas) {
                val checkPoint = saveLayer(null, null)
                drawRect(Color.Black.copy(alpha = 0.7f))
                
                // Cutout
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(scanRect.left, scanRect.top),
                    size = Size(scanRect.width, scanRect.height),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
                restoreToCount(checkPoint)
            }
            
            // Corner Markers (Green)
            val strokeWidth = 4.dp.toPx()
            val cornerLength = 40.dp.toPx()
            val greenColor = Color(0xFF22C55E)
            val cornerRadius = 16.dp.toPx()

            // Top Left
            drawArc(
                color = greenColor,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(scanRect.left, scanRect.top),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawLine(greenColor, Offset(scanRect.left + cornerRadius, scanRect.top), Offset(scanRect.left + cornerLength, scanRect.top), strokeWidth, cap = StrokeCap.Round)
            drawLine(greenColor, Offset(scanRect.left, scanRect.top + cornerRadius), Offset(scanRect.left, scanRect.top + cornerLength), strokeWidth, cap = StrokeCap.Round)

            // Top Right
            drawArc(
                color = greenColor,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(scanRect.right - cornerRadius * 2, scanRect.top),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawLine(greenColor, Offset(scanRect.right - cornerLength, scanRect.top), Offset(scanRect.right - cornerRadius, scanRect.top), strokeWidth, cap = StrokeCap.Round)
            drawLine(greenColor, Offset(scanRect.right, scanRect.top + cornerRadius), Offset(scanRect.right, scanRect.top + cornerLength), strokeWidth, cap = StrokeCap.Round)

            // Bottom Left
            drawArc(
                color = greenColor,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(scanRect.left, scanRect.bottom - cornerRadius * 2),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawLine(greenColor, Offset(scanRect.left + cornerRadius, scanRect.bottom), Offset(scanRect.left + cornerLength, scanRect.bottom), strokeWidth, cap = StrokeCap.Round)
            drawLine(greenColor, Offset(scanRect.left, scanRect.bottom - cornerLength), Offset(scanRect.left, scanRect.bottom - cornerRadius), strokeWidth, cap = StrokeCap.Round)

            // Bottom Right
            drawArc(
                color = greenColor,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(scanRect.right - cornerRadius * 2, scanRect.bottom - cornerRadius * 2),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawLine(greenColor, Offset(scanRect.right - cornerLength, scanRect.bottom), Offset(scanRect.right - cornerRadius, scanRect.bottom), strokeWidth, cap = StrokeCap.Round)
            drawLine(greenColor, Offset(scanRect.right, scanRect.bottom - cornerLength), Offset(scanRect.right, scanRect.bottom - cornerRadius), strokeWidth, cap = StrokeCap.Round)

            // Animated Scan Line
            val lineY = scanRect.top + (scanRect.height * scanLineProgress)
            drawLine(
                color = greenColor,
                start = Offset(scanRect.left + 10.dp.toPx(), lineY),
                end = Offset(scanRect.right - 10.dp.toPx(), lineY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onClose,
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.padding(8.dp))
            }

            Surface(
                onClick = onFlashToggle,
                shape = CircleShape,
                color = if (isFlashOn) Color.White else Color.Black.copy(alpha = 0.5f),
                contentColor = if (isFlashOn) Color.Black else Color.White
            ) {
                Icon(
                    if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Instruction (Top)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("Arahkan kamera ke barcode", color = Color.White, fontSize = 14.sp)
            }
        }

        // Tips (Center, below scan area)
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 280.dp),
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(
                    "Tips: Pastikan pencahayaan cukup\ndan barcode tidak buram",
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Bottom Actions
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScannerActionButton(icon = Icons.Default.Keyboard, label = "Input Manual", onClick = onManualInput)
                
                // Big Scan Button
                Surface(
                    onClick = { /* Could re-trigger focus or similar */ },
                    shape = CircleShape,
                    color = Color(0xFF025EB9),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                ScannerActionButton(icon = Icons.Default.Image, label = "Galeri", onClick = onGalleryClick)
            }
        }
    }
}

@Composable
fun ScannerActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

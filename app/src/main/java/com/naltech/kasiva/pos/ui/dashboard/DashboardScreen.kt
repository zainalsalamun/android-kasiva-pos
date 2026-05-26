package com.naltech.kasiva.pos.ui.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naltech.kasiva.pos.util.formatCurrency
import java.util.Locale

private val BlueAccent = Color(0xFF02569B)
private val LightBlue = Color(0xFF0EA5E9)
private val SoftBlueBg = Color(0xFFF8FAFC)
private val SuccessGreen = Color(0xFF22C55E)
private val WarningOrange = Color(0xFFF59E0B)
private val ErrorRed = Color(0xFFEF4444)
private val BorderColor = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Hi, Bang Zai", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Selamat datang di Kasiva POS", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                actions = {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
                            Spacer(Modifier.width(6.dp))
                            Text("Online", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = {}) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Spacer(Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SoftBlueBg
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
            val isCompact = maxWidth < 720.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isCompact) 12.dp else 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardSummaryCards(uiState, Modifier.weight(1f), Modifier.weight(1f), 0)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardSummaryCards(uiState, Modifier.weight(1f), Modifier.weight(1f), 2)
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardSummaryCards(uiState, Modifier.weight(1f), Modifier.weight(1f), 0)
                        DashboardSummaryCards(uiState, Modifier.weight(1f), Modifier.weight(1f), 2)
                    }
                }
            }

            item {
                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SalesChartCard(uiState.salesHistory, Modifier.fillMaxWidth().height(260.dp))
                        TopProductsCard(uiState.topProducts, Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(320.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SalesChartCard(uiState.salesHistory, Modifier.weight(1.5f).fillMaxHeight())
                        TopProductsCard(uiState.topProducts, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }

            // Recent Transactions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Transaksi Terakhir", fontWeight = FontWeight.Bold)
                            Text("Lihat semua", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        // Header Table
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(vertical = 8.dp, horizontal = 12.dp)) {
                            Text("No. Invoice", modifier = Modifier.weight(1.5f), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Waktu", modifier = Modifier.weight(1.5f), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Kasir", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Total", modifier = Modifier.weight(1.2f), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Metode", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Status", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }
                        
                        uiState.recentTransactions.forEach { transaction ->
                            TransactionRow(item = transaction)
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun RowScope.DashboardSummaryCards(
    uiState: DashboardUiState,
    firstModifier: Modifier,
    secondModifier: Modifier,
    startIndex: Int
) {
    val cards = listOf(
        Triple("Total Penjualan", "Rp 12.450.000", Icons.AutoMirrored.Filled.TrendingUp),
        Triple("Total Transaksi", uiState.transactionCount.toString(), Icons.Default.ShoppingCart),
        Triple("Produk", uiState.productCount.toString(), Icons.Default.Inventory2),
        Triple("Stok Menipis", uiState.lowStockCount.toString(), Icons.Default.Warning)
    )
    val subValues = listOf("▲ 12% dari kemarin", "▲ 8% dari kemarin", "Semua produk", "Perlu restock")
    val iconColors = listOf(LightBlue, SuccessGreen, Color(0xFF6D28D9), WarningOrange)
    val subColors = listOf(SuccessGreen, SuccessGreen, Color.Gray, ErrorRed)

    SummaryCard(
        title = cards[startIndex].first,
        value = cards[startIndex].second,
        subValue = subValues[startIndex],
        subValueColor = subColors[startIndex],
        icon = cards[startIndex].third,
        iconColor = iconColors[startIndex],
        modifier = firstModifier
    )
    SummaryCard(
        title = cards[startIndex + 1].first,
        value = cards[startIndex + 1].second,
        subValue = subValues[startIndex + 1],
        subValueColor = subColors[startIndex + 1],
        icon = cards[startIndex + 1].third,
        iconColor = iconColors[startIndex + 1],
        modifier = secondModifier
    )
}

@Composable
private fun SalesChartCard(points: List<SalesPoint>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Penjualan 7 Hari Terakhir", fontWeight = FontWeight.Bold)
                AssistChip(onClick = {}, label = { Text("7 Hari") }, trailingIcon = {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                })
            }
            Spacer(Modifier.height(12.dp))
            LineChart(points = points, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun TopProductsCard(products: List<TopProduct>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Produk Terlaris", fontWeight = FontWeight.Bold)
                Text("Lihat semua", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            products.take(5).forEachIndexed { index, product ->
                TopProductRow(rank = index + 1, product = product)
            }
        }
    }
}

@Composable
private fun LineChart(points: List<SalesPoint>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp)).padding(12.dp)) {
        if (points.isEmpty()) return@Canvas
        val left = 42.dp.toPx()
        val right = size.width - 16.dp.toPx()
        val top = 18.dp.toPx()
        val bottom = size.height - 30.dp.toPx()
        val maxValue = (points.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0)

        repeat(4) { index ->
            val y = top + (bottom - top) * index / 3f
            drawLine(Color(0xFFE2E8F0), androidx.compose.ui.geometry.Offset(left, y), androidx.compose.ui.geometry.Offset(right, y), 1.dp.toPx())
        }

        val coordinates = points.mapIndexed { index, point ->
            val x = left + (right - left) * index / (points.lastIndex.coerceAtLeast(1)).toFloat()
            val y = bottom - ((point.value / maxValue).toFloat() * (bottom - top))
            androidx.compose.ui.geometry.Offset(x, y)
        }
        val path = Path().apply {
            moveTo(coordinates.first().x, coordinates.first().y)
            coordinates.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, BlueAccent, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        coordinates.forEach { point ->
            drawCircle(Color.White, 5.dp.toPx(), point)
            drawCircle(BlueAccent, 3.dp.toPx(), point)
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    subValueColor: Color = Color.Gray
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = iconColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Icon(icon, contentDescription = null, modifier = Modifier.padding(6.dp).size(20.dp), tint = iconColor)
                }
                Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Column {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(subValue, fontSize = 11.sp, color = subValueColor, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun TopProductRow(rank: Int, product: TopProduct) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            modifier = Modifier.width(24.dp),
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.LightGray)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${product.soldCount} terjual", fontSize = 11.sp, color = Color.Gray)
        }
        Text(
            text = String.format(Locale.US, "Rp %.0f", product.totalRevenue),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun TransactionRow(item: TransactionItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.invoice, modifier = Modifier.weight(1.5f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(item.time, modifier = Modifier.weight(1.5f), fontSize = 13.sp, color = Color.Gray)
        Text(item.cashier, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Text(String.format(Locale.US, "Rp %.0f", item.amount), modifier = Modifier.weight(1.2f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(item.method, modifier = Modifier.weight(1f), fontSize = 13.sp)
        
        val statusColor = if (item.status == "Tersync") SuccessGreen else WarningOrange
        Surface(
            modifier = Modifier.weight(1f),
            color = statusColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                item.status,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun DashboardScreenPreview() {
    // Mock ViewModel would be needed for a real preview, or just use the UI components directly
}

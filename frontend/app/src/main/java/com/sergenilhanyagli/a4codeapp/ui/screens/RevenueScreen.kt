package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(nav: NavHostController) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("Günlük") }
    var totalRevenue by remember { mutableStateOf(0.0) }
    var totalOrders by remember { mutableStateOf(0) }

    val ranges = listOf("Günlük", "Haftalık", "Aylık")

    // 🔹 API'den verileri getir
    LaunchedEffect(selectedRange) {
        scope.launch {
            try {
                val response: List<Map<String, Any>> = when (selectedRange) {
                    "Haftalık" -> ApiClient.instance.getSalesWeekly()
                    "Aylık" -> ApiClient.instance.getSalesMonthly()
                    else -> ApiClient.instance.getSalesDaily()
                } as List<Map<String, Any>>

                val list = response ?: emptyList()
                totalRevenue = list.fold(0.0) { acc, item ->
                    acc + ((item["total"] as? Number)?.toDouble() ?: 0.0)
                }
                totalOrders = list.size
            } catch (e: Exception) {
                totalRevenue = 0.0
                totalOrders = 0
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ciro Raporu", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFB49AF7), Color(0xFFD7C8FA))
                    )
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF7F4FF), Color(0xFFFFFFFF))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 🔽 Zaman Aralığı Seçimi
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedRange,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Zaman Aralığı") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ranges.forEach { range ->
                        DropdownMenuItem(
                            text = { Text(range) },
                            onClick = {
                                selectedRange = range
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 🔹 Bilgi Kartları
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoCard(
                    title = "Toplam Ciro",
                    value = "%.2f ₺".format(totalRevenue),
                    color = Color(0xFF9C8DF5),
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    title = "Toplam Sipariş",
                    value = "$totalOrders adet",
                    color = Color(0xFFBBA9F6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // 🔹 Boş durum mesajı
            if (totalOrders == 0) {
                Text(
                    text = "Bu dönemde kayıtlı satış bulunamadı",
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = color)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

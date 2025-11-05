package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(nav: NavHostController) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("Günlük") }
    var totalRevenue by remember { mutableStateOf(0.0) }
    var totalOrders by remember { mutableStateOf(0) }
    var sales by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    val ranges = listOf("Günlük", "Haftalık", "Aylık")

    LaunchedEffect(selectedRange) {
        scope.launch {
            try {
                val list = when (selectedRange) {
                    "Haftalık" -> ApiClient.instance.getSalesWeekly().body() ?: emptyList()
                    "Aylık" -> ApiClient.instance.getSalesMonthly().body() ?: emptyList()
                    else -> ApiClient.instance.getSalesDaily().body() ?: emptyList()
                }

                // 🔹 Ciro toplamı
                totalRevenue = list.sumOf { (it["total"] as? Number)?.toDouble() ?: 0.0 }

                // 🔹 Gerçek sipariş sayısı
                val allSales = ApiClient.instance.getSales().body() ?: emptyList()
                totalOrders = allSales.size  // ✅ Tüm satış kayıtlarını say

                // 🔹 Satış listesini güncelle
                sales = allSales.sortedByDescending { it["createdAt"].toString() }

            } catch (e: Exception) {
                totalRevenue = 0.0
                totalOrders = 0
                sales = emptyList()
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ciro Raporu", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF7B61FF))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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

            Divider(Modifier.padding(vertical = 8.dp))

            // 🔹 Satış Listesi (Tamamı)
            Text("📜 Satış Geçmişi", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sales.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Henüz satış yapılmadı", color = Color.Gray)
                        }
                    }
                } else {
                    items(sales) { sale ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 🔹 Satıcı adını düzgün göster
                                val userMap = sale["user"] as? Map<*, *>
                                val sellerName = userMap?.get("name")?.toString() ?: "Bilinmiyor"

                                // 🔹 Tarihi biçimlendir
                                val rawDate = sale["createdAt"]?.toString() ?: ""
                                val formattedDate = try {
                                    val inputFormat = SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                        Locale.getDefault()
                                    )
                                    val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                    val parsed = inputFormat.parse(rawDate)
                                    outputFormat.format(parsed ?: "")
                                } catch (e: Exception) {
                                    rawDate
                                }

                                Text("👤 Satıcı: $sellerName", fontWeight = FontWeight.Medium, color = Color(0xFF4A3AFF))
                                Text("💰 Tutar: ${sale["totalPrice"] ?: 0} ₺", fontSize = 15.sp)
                                Text("💳 Ödeme: ${sale["paymentType"] ?: "Bilinmiyor"}", fontSize = 15.sp)
                                Text("📅 Tarih: $formattedDate", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                        }
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
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = color)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
    var sales by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val ranges = listOf("Günlük", "Haftalık", "Aylık")

    LaunchedEffect(selectedRange) {
        scope.launch {
            try {
                val response = when (selectedRange) {
                    "Haftalık" -> ApiClient.instance.getSalesWeekly()
                    "Aylık" -> ApiClient.instance.getSalesMonthly()
                    else -> ApiClient.instance.getSalesDaily()
                }

                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    sales = list

                    totalRevenue = list.fold(0.0) { acc, item ->
                        acc + ((item["total"] as? Number)?.toDouble() ?: 0.0)
                    }
                } else {
                    sales = emptyList()
                    totalRevenue = 0.0
                }
            } catch (e: Exception) {
                sales = emptyList()
                totalRevenue = 0.0
            }
        }
    }

    val filteredSales = sales.filter {
        val userName = (it["user"] as? Map<*, *>)?.get("name")?.toString()?.lowercase() ?: ""
        val productName = it["product"]?.toString()?.lowercase() ?: ""
        searchQuery.lowercase() in userName || searchQuery.lowercase() in productName
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ciro ve Satışlar", fontWeight = FontWeight.SemiBold) },
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

            // 🔹 Ciro Kartı
            InfoCard(
                title = "Toplam Ciro",
                value = "%.2f ₺".format(totalRevenue),
                color = Color(0xFF9C8DF5),
                modifier = Modifier.fillMaxWidth()
            )

            // 🔍 Arama Alanı
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
                label = { Text("Satıcı veya ürün ara") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(Modifier.height(8.dp))

            // 🔹 Satış Listesi
            if (filteredSales.isEmpty()) {
                Text("Sonuç bulunamadı", color = Color.Gray)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSales) { sale ->
                        SaleItemCard(sale)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.height(100.dp)
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

@Composable
fun SaleItemCard(sale: Map<String, Any>) {
    val userName = (sale["user"] as? Map<*, *>)?.get("name")?.toString() ?: "Bilinmiyor"
    val total = (sale["totalPrice"] as? Number)?.toDouble() ?: 0.0
    val createdAt = sale["createdAt"]?.toString()?.take(10) ?: "Tarih yok"
    val payment = sale["paymentType"]?.toString() ?: "Bilinmiyor"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1EDFE)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val userName = (sale["user"] as? Map<*, *>)?.get("name")?.toString() ?: "Bilinmiyor"
            val total = (sale["totalPrice"] as? Number)?.toDouble() ?: 0.0
            val payment = sale["paymentType"]?.toString() ?: "Bilinmiyor"
            val createdAt = sale["createdAt"]?.toString()?.take(10) ?: "Tarih yok"

        }
    }
}

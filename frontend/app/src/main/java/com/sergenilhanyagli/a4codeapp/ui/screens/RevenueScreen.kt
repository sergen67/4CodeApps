package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen() {
    val scope = rememberCoroutineScope()
    var daily by remember { mutableStateOf(0.0) }
    var weekly by remember { mutableStateOf(0.0) }
    var monthly by remember { mutableStateOf(0.0) }
    var sales by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val d = ApiClient.instance.getSalesDaily()
            if (d.isSuccessful) daily = (d.body()?.firstOrNull()?.get("total") as? Number)?.toDouble() ?: 0.0

            val w = ApiClient.instance.getSalesWeekly()
            if (w.isSuccessful) weekly = w.body()?.sumOf { (it["total"] as? Number)?.toDouble() ?: 0.0 } ?: 0.0

            val m = ApiClient.instance.getSalesMonthly()
            if (m.isSuccessful) monthly = m.body()?.sumOf { (it["total"] as? Number)?.toDouble() ?: 0.0 } ?: 0.0

            val s = ApiClient.instance.getSales()
            if (s.isSuccessful) sales = s.body() ?: emptyList()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF6F3FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ciro Paneli", color = Color.White, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF7B61FF))
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 🔹 Ciro Kartları
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RevenueCard("Günlük", daily, Modifier.weight(1f))
                RevenueCard("Haftalık", weekly, Modifier.weight(1f))
                RevenueCard("Aylık", monthly, Modifier.weight(1f))
            }

            Text(
                "Satış Geçmişi",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3AFF),
                modifier = Modifier.padding(top = 8.dp)
            )

            if (sales.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz satış yapılmadı", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(sales) { sale ->
                        SaleItemCard(sale)
                    }
                }
            }
        }
    }
}

/* 🔹 Kart tasarımı */
@Composable
fun RevenueCard(title: String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color(0xFF7B61FF), fontWeight = FontWeight.Medium)
            Text("%.2f ₺".format(amount), fontWeight = FontWeight.Bold, color = Color(0xFF4A3AFF))
        }
    }
}

/* 🔹 Satış listesi kartı */
@Composable
fun SaleItemCard(sale: Map<String, Any>) {
    val date = sale["createdAt"]?.toString()?.let {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val parsed = parser.parse(it)
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(parsed!!)
        } catch (_: Exception) { "-" }
    } ?: "-"

    val total = (sale["totalPrice"] as? Number)?.toDouble() ?: 0.0
    val payment = sale["paymentType"]?.toString() ?: "-"
    val user = (sale["user"] as? Map<*, *>)?.get("name")?.toString() ?: "Bilinmiyor"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Satıcı: $user", fontWeight = FontWeight.Medium, color = Color(0xFF4A3AFF))
            Spacer(Modifier.height(4.dp))
            Text("Tarih: $date", color = Color.Gray)
            Text("Ödeme: $payment", color = Color(0xFF6B6B6B))
            Text("Tutar: %.2f ₺".format(total), color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

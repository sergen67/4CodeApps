package com.sergenilhanyagli.a4codeapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import kotlinx.coroutines.launch

@Composable
fun RevenueScreen(nav: NavHostController) {
    var dailyRevenue by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var monthlyRevenue by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var sales by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val all = ApiClient.instance.getSales()
            if (all.isSuccessful) sales = all.body() ?: emptyList()

            val daily = ApiClient.instance.getDailyRevenue()
            if (daily.isSuccessful) dailyRevenue = daily.body() ?: emptyList()

            val monthly = ApiClient.instance.getMonthlyRevenue()
            if (monthly.isSuccessful) monthlyRevenue = monthly.body() ?: emptyList()
        } catch (e: Exception) {
            Log.e("4CODEAPP", "Ciro verisi alınamadı: ${e.message}")
        }
    }

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💰 Günlük Ciro", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.height(150.dp)) {
            items(dailyRevenue) { row ->
                Text("${row["date"]}: ${row["total"]} ₺")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("📆 Aylık Ciro", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.height(150.dp)) {
            items(monthlyRevenue) { row ->
                Text("${row["month"]}: ${row["total"]} ₺")
            }
        }

        Divider(Modifier.padding(vertical = 16.dp))

        Text("🧾 Satış Geçmişi", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(sales) { s ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Satıcı: ${(s["user"] as Map<*, *>)["name"]}")
                        Text("Tutar: ${s["totalPrice"]} ₺")
                        Text("Ödeme: ${s["paymentType"]}")
                        Text("Tarih: ${s["createdAt"]}")
                    }
                }
            }
        }
    }
}

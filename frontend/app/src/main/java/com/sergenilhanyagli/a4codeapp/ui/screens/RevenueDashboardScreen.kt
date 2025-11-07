package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import kotlinx.coroutines.launch

@Composable
fun RevenueDashboardScreen() {
    val scope = rememberCoroutineScope()
    var daily by remember { mutableStateOf(0.0) }
    var weekly by remember { mutableStateOf(0.0) }
    var monthly by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        scope.launch {
            // Daily
            val d = ApiClient.instance.getDailyRevenue()
            if (d.isSuccessful) {
                daily = (d.body()?.firstOrNull()?.get("total") as? Number)?.toDouble() ?: 0.0
            }
            // Weekly
            val w = ApiClient.instance.getSalesWeekly()
            if (w.isSuccessful) {
                weekly = (w.body()?.firstOrNull()?.get("total") as? Number)?.toDouble() ?: 0.0
            }
            // Monthly
            val m = ApiClient.instance.getMonthlyRevenue()
            if (m.isSuccessful) {
                monthly = (m.body()?.firstOrNull()?.get("total") as? Number)?.toDouble() ?: 0.0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Ciro Özeti",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DashboardRevenueCard("Günlük", daily, Modifier.weight(1f))
            DashboardRevenueCard("Haftalık", weekly, Modifier.weight(1f))
            DashboardRevenueCard("Aylık", monthly, Modifier.weight(1f))
        }
    }
}

@Composable
fun DashboardRevenueCard(title: String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Text("%.2f ₺".format(amount), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

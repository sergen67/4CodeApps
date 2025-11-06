package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.viewmodel.MainViewModel
import com.sergenilhanyagli.a4codeapp.data.models.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(nav: NavHostController, currentUser: User, vm: MainViewModel) {
    val cartItems = vm.cartItems
    val total = vm.totalPrice()
    val scope = rememberCoroutineScope()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF6F3FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sepetim", color = Color.White, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF7B61FF))
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = Color(0xFFEDE7FF),
                    shadowElevation = 6.dp,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toplam: %.2f ₺".format(total),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A3AFF)
                        )
                        Button(
                            onClick = { showPaymentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ödemeye Geç", color = Color.White)
                        }
                    }
                }
            }
        }
    ) { pad ->
        if (cartItems.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) {
                Text("Sepetiniz boş", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.product.name, color = Color(0xFF4A3AFF), fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Text("Fiyat: %.2f ₺".format(item.product.price), color = Color.Gray)
                                Text("Adet: ${item.quantity}", color = Color(0xFF6B6B6B))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { vm.removeFromCart(item.product) },
                                    shape = RoundedCornerShape(50),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp, brush = SolidColor(
                                        Color(0xFF7B61FF)
                                    )
                                    )
                                ) { Text("-", color = Color(0xFF7B61FF), fontWeight = FontWeight.Bold) }

                                OutlinedButton(
                                    onClick = { vm.addToCart(item.product) },
                                    shape = RoundedCornerShape(50),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp, brush = SolidColor(Color(0xFF7B61FF)))
                                ) { Text("+", color = Color(0xFF7B61FF), fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
        }
    }

    // 🔹 Ödeme tipi seçimi
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Ödeme Türü Seç", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Kart", "Nakit").forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPayment = type
                                    showPaymentDialog = false
                                    scope.launch {
                                        val success = vm.completeSale(type)
                                        message = if (success) "✅ Ödeme başarıyla alındı" else "❌ Ödeme başarısız"
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedPayment == type,
                                onClick = {
                                    selectedPayment = type
                                    showPaymentDialog = false
                                    scope.launch {
                                        val success = vm.completeSale(type)
                                        message = if (success) "✅ Ödeme başarıyla alındı" else "❌ Ödeme başarısız"
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7B61FF))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(type)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("İptal") }
            }
        )
    }

    // 🔹 Bilgilendirme mesajı
    if (message.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { message = "" },
            title = { Text("Bilgi") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { message = "" }) { Text("Tamam") }
            }
        )
    }
}

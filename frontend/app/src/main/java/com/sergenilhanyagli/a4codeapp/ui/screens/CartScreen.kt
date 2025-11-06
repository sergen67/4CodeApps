package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sepetim", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri Dön", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF7B61FF)
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Toplam: %.2f ₺".format(total),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ödemeye Geç")
                        }

                        OutlinedButton(
                            onClick = { vm.clearCart() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF7B61FF))
                        ) {
                            Text("Sepeti Sil", color = Color(0xFF7B61FF))
                        }
                    }
                }
            }
        }
    ) { pad ->
        if (cartItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Sepetiniz boş", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.product.name, color = Color(0xFF4A3AFF))
                                Spacer(Modifier.height(4.dp))
                                Text("Fiyat: %.2f ₺".format(item.product.price))
                                Text("Adet: ${item.quantity}")
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { vm.removeFromCart(item.product) },
                                    shape = RoundedCornerShape(50)
                                ) { Text("-") }

                                OutlinedButton(
                                    onClick = { vm.addToCart(item.product) },
                                    shape = RoundedCornerShape(50)
                                ) { Text("+") }
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
            title = { Text("Ödeme Türü Seç") },
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
                                        if (success) {
                                            vm.clearCart() // 🔹 Sepeti sıfırla
                                            nav.navigate("products") { // 🔹 Ürün listesine dön
                                                popUpTo("cart") { inclusive = true }
                                            }
                                        } else {
                                            message = "❌ Ödeme başarısız"
                                        }
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
                                        if (success) {
                                            vm.clearCart()
                                            nav.navigate("products") {
                                                popUpTo("cart") { inclusive = true }
                                            }
                                        } else {
                                            message = "❌ Ödeme başarısız"
                                        }
                                    }
                                }
                            )
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

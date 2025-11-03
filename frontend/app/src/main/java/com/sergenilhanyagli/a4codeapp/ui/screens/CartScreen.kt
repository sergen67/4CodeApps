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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.Product
import com.sergenilhanyagli.a4codeapp.data.models.User
import com.sergenilhanyagli.a4codeapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    nav: NavHostController,
    currentUser: User,
    vm: MainViewModel // 🔹 Artık dışarıdan geliyor
)
{
    val scope = rememberCoroutineScope()
    val cartItems = vm.cartItems
    val totalPrice = cartItems.sumOf { it.price }

    var selectedPaymentType by remember { mutableStateOf("kart") }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sepet") }) }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (cartItems.isEmpty()) {
                Text("Sepet boş.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cartItems) { product ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(product.name)
                            Text("${product.price} ₺")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Toplam: ${"%.2f".format(totalPrice)} ₺")

                Row(horizontalArrangement = Arrangement.Center) {
                    RadioButton(
                        selected = selectedPaymentType == "kart",
                        onClick = { selectedPaymentType = "kart" }
                    )
                    Text("Kart")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedPaymentType == "nakit",
                        onClick = { selectedPaymentType = "nakit" }
                    )
                    Text("Nakit")
                }

                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    scope.launch {
                        try {
                            val body = hashMapOf<String, Any>(
                                "userId" to (currentUser.id ?: 0),
                                "totalPrice" to totalPrice.toDouble(),
                                "paymentType" to selectedPaymentType
                            )
                            val res = ApiClient.instance.createSale(body)
                            if (res.isSuccessful) {
                                message = "✅ Ödeme tamamlandı."
                                vm.clearCart()
                            } else {
                                message = "❌ Hata: ${res.code()}"
                            }
                        } catch (e: Exception) {
                            message = "❌ Hata: ${e.message}"
                        }
                    }
                }) {
                    Text("Ödemeyi Tamamla")
                }

                Spacer(Modifier.height(12.dp))
                if (message.isNotEmpty()) Text(message)
            }
        }
    }
}

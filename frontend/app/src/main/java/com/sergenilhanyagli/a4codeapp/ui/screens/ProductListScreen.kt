package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.Product
import com.sergenilhanyagli.a4codeapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(nav: NavHostController, vm: MainViewModel) {
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // 🔹 Kategorileri ve ürünleri yükle
    LaunchedEffect(Unit) {
        scope.launch {
            val catRes = ApiClient.instance.getCategories()
            if (catRes.isSuccessful) categories = catRes.body() ?: emptyList()

            val prodRes = ApiClient.instance.getProducts()
            if (prodRes.isSuccessful) products = prodRes.body() ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ürün Satış Ekranı", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF7B61FF)
                )
            )
        },
        bottomBar = {
            if (vm.cartItems.isNotEmpty()) {
                BottomAppBar(containerColor = Color(0xFF7B61FF), contentColor = Color.White) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sepet: ${vm.cartItems.size} ürün | %.2f ₺".format(vm.totalPrice()))
                        Button(
                            onClick = { nav.navigate("cart/${vm.totalPrice()}") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Sepete Git", color = Color(0xFF7B61FF))
                        }
                    }
                }
            }
        }
    ) { pad ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // 🔹 Sol dikey kategori menüsü
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp)
                    .background(Color(0xFFF6F3FF))
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val name = category["name"].toString()
                        val isSelected = name == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF7B61FF) else Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = name }
                        ) {
                            Box(
                                Modifier
                                    .padding(12.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // 🔹 Sağ taraf (ürün listesi)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // ✅ categoryId bazlı filtreleme
                val filteredProducts = if (selectedCategory == null) {
                    products
                } else {
                    val selectedCatId = categories.find { it["name"] == selectedCategory }?.get("id")
                    val selectedId = (selectedCatId as? Double)?.toInt() ?: -1
                    products.filter { it.categoryId == selectedId }
                }

                if (filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bu kategoride ürün bulunmuyor", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredProducts) { product ->
                            val quantity = vm.cartItems.find { it.product.id == product.id }?.quantity ?: 0

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(product.name, color = Color(0xFF4A3AFF))
                                    Spacer(Modifier.height(4.dp))
                                    Text("%.2f ₺".format(product.price))
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = { vm.addToCart(product) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Sepete Ekle")
                                    }
                                    // 🔹 Yeni eklenen + / - satırı
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedButton(
                                            onClick = { vm.removeFromCart(product) },
                                            shape = RoundedCornerShape(50),
                                            border = ButtonDefaults.outlinedButtonBorder,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("-")
                                        }

                                        Text(
                                            text = "$quantity",
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        OutlinedButton(
                                            onClick = { vm.addToCart(product) },
                                            shape = RoundedCornerShape(50),
                                            border = ButtonDefaults.outlinedButtonBorder,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("+")
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

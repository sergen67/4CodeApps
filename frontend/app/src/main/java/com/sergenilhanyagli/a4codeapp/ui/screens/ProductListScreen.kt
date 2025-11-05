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
import androidx.compose.ui.text.font.FontWeight
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
    var showHelvaSheet by remember { mutableStateOf<Product?>(null) }

    // 🔹 API'den kategorileri ve ürünleri yükle
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
                title = { Text("Satış Ekranı", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF7B61FF)
                )
            )
        },
        bottomBar = {
            if (vm.cartItems.isNotEmpty()) {
                Surface(
                    color = Color(0xFFEDE7FF),
                    shadowElevation = 5.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Toplam: %.2f ₺".format(vm.totalPrice()),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A3AFF)
                        )
                        Button(
                            onClick = { nav.navigate("cart") },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ödemeye Geç")
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
            // 🔹 Sol kategori menüsü
            Surface(
                color = Color(0xFFF6F3FF),
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val name = cat["name"].toString()
                        val selected = name == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color(0xFF7B61FF) else Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = name }
                        ) {
                            Box(
                                Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    name,
                                    color = if (selected) Color.White else Color.Black
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
                val filteredProducts = if (selectedCategory == null)
                    products
                else
                    products.filter { it.category == selectedCategory }

                if (filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bu kategoride ürün yok", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredProducts) { product ->
                            // 🔸 Helvalar
                            if (product.category == "Helvalar") {
                                HelvaCard(product) { selected ->
                                    showHelvaSheet = selected
                                }
                            } else {
                                // 🔸 Normal ürün kartı
                                NormalProductCard(vm, product)
                            }
                        }
                    }
                }
            }
        }
    }

    // 🔹 Helva varyant seçim ekranı
    showHelvaSheet?.let { helva ->
        ModalBottomSheet(
            onDismissRequest = { showHelvaSheet = null },
            containerColor = Color(0xFFFFF4E0),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${helva.name} Seçenekleri",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6E4A00)
                )
                Divider(color = Color(0xFFE0C097))

                val variants = helva.variants ?: emptyList()
                variants.forEach { v ->
                    val vName = v["name"]?.toString() ?: "-"
                    val vPrice = (v["price"] as? Number)?.toDouble() ?: 0.0

                    Surface(
                        color = Color(0xFFFFEAB5),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.addToCart(
                                    helva.copy(
                                        name = "${helva.name} - $vName",
                                        price = vPrice
                                    )
                                )
                                showHelvaSheet = null
                            }
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(vName, color = Color(0xFF5E3A00))
                            Text("%.2f ₺".format(vPrice))
                        }
                    }
                }

                Button(
                    onClick = { showHelvaSheet = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C6615)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kapat", color = Color.White)
                }
            }
        }
    }
}

/* 🔹 Normal Ürün Kartı */
@Composable
fun NormalProductCard(vm: MainViewModel, product: Product) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(product.name, color = Color(0xFF4A3AFF), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text("%.2f ₺".format(product.price))
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = { vm.removeFromCart(product) }) { Text("-") }
                Button(onClick = { vm.addToCart(product) }) { Text("Sepete Ekle") }
                OutlinedButton(onClick = { vm.addToCart(product) }) { Text("+") }
            }
        }
    }
}

/* 🔹 Helva Ürün Kartı */
@Composable
fun HelvaCard(product: Product, onSelect: (Product) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E7)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(product) }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(product.name, color = Color(0xFF9C6615), fontWeight = FontWeight.Bold)
            Text("Tıklayarak varyant seç", color = Color.Gray, fontSize = MaterialTheme.typography.labelMedium.fontSize)
        }
    }
}

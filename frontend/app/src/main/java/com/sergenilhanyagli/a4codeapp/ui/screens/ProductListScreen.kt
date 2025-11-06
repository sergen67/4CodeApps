package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("Tümü") }
    var searchQuery by remember { mutableStateOf("") }
    var showHelvaSheet by remember { mutableStateOf<Product?>(null) }

    // 🔹 Verileri yükle
    LaunchedEffect(Unit) {
        scope.launch {
            val catRes = ApiClient.instance.getCategories()
            if (catRes.isSuccessful) {
                val loaded = catRes.body() ?: emptyList()
                categories = listOf(mapOf("name" to "Tümü")) + loaded
            }
            val prodRes = ApiClient.instance.getProducts()
            if (prodRes.isSuccessful) products = prodRes.body() ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Satış Ekranı", color = Color.White) },
                actions = {
                    IconButton(onClick = {
                        vm.logout(context)
                        nav.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF7B61FF))
            )
        },
        bottomBar = {
            if (vm.cartItems.isNotEmpty()) {
                Surface(color = Color(0xFFEDE7FF)) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Toplam: %.2f ₺".format(vm.totalPrice()),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A3AFF)
                        )
                        Button(onClick = { nav.navigate("cart") }, shape = RoundedCornerShape(10.dp)) {
                            Text("Ödemeye Geç")
                        }
                    }
                }
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(12.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                placeholder = { Text("Ürün ara...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            )

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val name = cat["name"].toString()
                    val selected = name == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Color(0xFF7B61FF) else Color.White,
                        modifier = Modifier.clickable { selectedCategory = name }
                    ) {
                        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                name,
                                color = if (selected) Color.White else Color(0xFF4A3AFF),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            val filteredProducts = products.filter {
                (selectedCategory == "Tümü" || it.category == selectedCategory) &&
                        it.name.contains(searchQuery, true)
            }

            if (filteredProducts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Eşleşen ürün bulunamadı", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredProducts) { product ->
                        if (product.category == "Helvalar") {
                            HelvaCard(product) { showHelvaSheet = it }
                        } else {
                            NormalProductCard(vm, product)
                        }
                    }
                }
            }
        }
    }

    showHelvaSheet?.let { helva ->
        ModalBottomSheet(onDismissRequest = { showHelvaSheet = null }) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("${helva.name} Seçenekleri", fontWeight = FontWeight.Bold)
                Divider()
                val variants = helva.variants ?: emptyList()
                variants.forEach { v ->
                    val vName = v["name"]?.toString() ?: "-"
                    val vPrice = (v["price"] as? Number)?.toDouble() ?: 0.0
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEAB5),
                        modifier = Modifier.fillMaxWidth().clickable {
                            vm.addToCart(helva.copy(name = "${helva.name} - $vName", price = vPrice))
                            showHelvaSheet = null
                        }
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(vName)
                            Text("%.2f ₺".format(vPrice))
                        }
                    }
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
            Text(
                "Tıklayarak varyant seç",
                color = Color.Gray,
                fontSize = MaterialTheme.typography.labelMedium.fontSize
            )
        }
    }
}

package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagerScreen(nav: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ürün Ekle", "Ürün Listesi")

    Column(modifier = Modifier.fillMaxSize()) {
        // 🔹 Başlık + sekmeler
        CenterAlignedTopAppBar(title = { Text("Ürün Yönetimi", fontWeight = FontWeight.SemiBold) })
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // 🔹 İçerik alanı
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            when (selectedTab) {
                0 -> ProductAddTab()
                1 -> ProductListTab()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAddTab(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        val res = ApiClient.instance.getCategories()
        if (res.isSuccessful) categories = res.body() ?: emptyList()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { pad ->
        Column(
            modifier = modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ürün Adı") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Fiyat") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategoryName ?: "Kategori Seç",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat["name"].toString()) },
                            onClick = {
                                selectedCategoryName = cat["name"].toString()
                                selectedCategoryId = (cat["id"] as? Double)?.toInt()
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val body = hashMapOf<String, Any>(
                                "name" to name,
                                "price" to (price.toDoubleOrNull() ?: 0.0)
                            )
                            if (selectedCategoryId != null) {
                                body["categoryId"] = selectedCategoryId!!
                            }

                            val res = ApiClient.instance.createProduct(HashMap(body))
                            if (res.isSuccessful) {
                                snackbarHost.showSnackbar("✅ Ürün başarıyla eklendi")
                                name = ""
                                price = ""
                                selectedCategoryName = null
                            } else {
                                snackbarHost.showSnackbar("❌ Hata: ${res.code()}")
                            }
                        } catch (e: Exception) {
                            snackbarHost.showSnackbar("⚠️ Hata: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ürünü Kaydet")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListTab(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var filteredProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var editProduct by remember { mutableStateOf<Product?>(null) }

    suspend fun loadProducts() {
        val res = ApiClient.instance.getProducts()
        if (res.isSuccessful) {
            products = res.body() ?: emptyList()
            filteredProducts = products
        }
    }

    LaunchedEffect(Unit) {
        loadProducts()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // 🔍 Arama alanı
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filteredProducts = if (it.isBlank()) {
                    products
                } else {
                    products.filter { p ->
                        p.name.contains(it, ignoreCase = true)
                    }
                }
            },
            label = { Text("Ürün Ara") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") }
        )

        if (filteredProducts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ürün bulunamadı")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text("${product.price} ₺", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { editProduct = product },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Düzenle") }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            ApiClient.instance.deleteProduct(product.id)
                                            loadProducts()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Sil") }
                            }
                        }
                    }
                }
            }
        }
    }

    // ✏️ Düzenleme dialogu (değişmedi)
    editProduct?.let { product ->
        var newName by remember { mutableStateOf(product.name) }
        var newPrice by remember { mutableStateOf(product.price.toString()) }

        AlertDialog(
            onDismissRequest = { editProduct = null },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val body = hashMapOf<String, Any>(
                            "name" to newName,
                            "price" to (newPrice.toDoubleOrNull() ?: 0.0)
                        )
                        ApiClient.instance.updateProduct(product.id, HashMap(body))
                        loadProducts()
                        editProduct = null
                    }
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { editProduct = null }) { Text("İptal") }
            },
            title = { Text("Ürünü Düzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Ürün Adı") })
                    OutlinedTextField(value = newPrice, onValueChange = { newPrice = it }, label = { Text("Fiyat") })
                }
            }
        )
    }
}


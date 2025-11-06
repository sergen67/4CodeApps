package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.Product
import com.sergenilhanyagli.a4codeapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagerScreen(nav: NavHostController) {
    val vm: MainViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ürün Ekle", "Ürün Listesi")

    Scaffold(
        containerColor = Color(0xFFF6F3FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ürün Yönetimi", color = Color.White, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF7B61FF))
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFFEDE7FF)) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if (selectedTab == index) Color(0xFF4A3AFF) else Color.DarkGray) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ProductAddTabModern()
                1 -> ProductListTabModern()
            }
        }
    }
}

/* 🔹 Ürün Ekleme Sekmesi */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAddTabModern(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    // 🔹 Kategorileri yükle
    LaunchedEffect(Unit) {
        val res = ApiClient.instance.getCategories()
        if (res.isSuccessful) categories = res.body() ?: emptyList()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = Color(0xFFF6F3FF)
    ) { pad ->
        Column(
            modifier = modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Kaydırılabilir içerik
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 🔹 Kategori seçimi
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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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

                // 🔹 Helva kategorisi özel form
                if (selectedCategoryName == "Helvalar") {
                    Text("Helva Ürün Ekleme", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Helva Adı") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    var small by remember { mutableStateOf("") }
                    var large by remember { mutableStateOf("") }
                    var smallIce by remember { mutableStateOf("") }
                    var largeIce by remember { mutableStateOf("") }

                    OutlinedTextField(value = small, onValueChange = { small = it }, label = { Text("Küçük Boy Fiyat") })
                    OutlinedTextField(value = large, onValueChange = { large = it }, label = { Text("Büyük Boy Fiyat") })
                    OutlinedTextField(value = smallIce, onValueChange = { smallIce = it }, label = { Text("Küçük Dondurmalı Fiyat") })
                    OutlinedTextField(value = largeIce, onValueChange = { largeIce = it }, label = { Text("Büyük Dondurmalı Fiyat") })

                    Spacer(Modifier.height(8.dp))

                    // 🔹 Kaydet butonu
                    Button(
                        onClick = {
                            scope.launch {
                                if (selectedCategoryId == null || name.isEmpty()) {
                                    snackbarHost.showSnackbar("⚠️ Lütfen kategori ve ürün adını doldurun")
                                    return@launch
                                }

                                val body = hashMapOf<String, Any>(
                                    "name" to name,
                                    "categoryId" to selectedCategoryId!!,
                                    "price" to 0.0,
                                    "variants" to listOf(
                                        hashMapOf("name" to "Küçük", "price" to (small.toDoubleOrNull() ?: 0.0)),
                                        hashMapOf("name" to "Büyük", "price" to (large.toDoubleOrNull() ?: 0.0)),
                                        hashMapOf("name" to "Küçük Dondurmalı", "price" to (smallIce.toDoubleOrNull() ?: 0.0)),
                                        hashMapOf("name" to "Büyük Dondurmalı", "price" to (largeIce.toDoubleOrNull() ?: 0.0))
                                    )
                                )

                                val res = ApiClient.instance.createProduct(HashMap(body))
                                if (res.isSuccessful) {
                                    snackbarHost.showSnackbar("✅ Helva varyasyonlarıyla eklendi")
                                    name = ""
                                    small = ""
                                    large = ""
                                    smallIce = ""
                                    largeIce = ""
                                } else {
                                    snackbarHost.showSnackbar("❌ Hata: ${res.code()}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                    ) { Text("Helva Ürününü Kaydet", color = Color.White) }
                } else {
                    // 🔹 Normal ürün formu
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ürün Adı") })
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Fiyat") })

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (selectedCategoryId == null || name.isEmpty()) {
                                    snackbarHost.showSnackbar("⚠️ Lütfen kategori ve ürün adını doldurun")
                                    return@launch
                                }

                                val body = hashMapOf<String, Any>(
                                    "name" to name,
                                    "price" to (price.toDoubleOrNull() ?: 0.0),
                                    "categoryId" to selectedCategoryId!!
                                )

                                val res = ApiClient.instance.createProduct(HashMap(body))
                                if (res.isSuccessful) {
                                    snackbarHost.showSnackbar("✅ Ürün başarıyla eklendi")
                                    name = ""
                                    price = ""
                                    selectedCategoryName = null
                                } else {
                                    snackbarHost.showSnackbar("❌ Hata: ${res.code()}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                    ) { Text("Ürünü Kaydet", color = Color.White) }
                }
            }
        }
    }
}


/* 🔹 Ürün Listesi Sekmesi */
@Composable
fun ProductListTabModern(modifier: Modifier = Modifier, vm: MainViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf<Product?>(null) }

    suspend fun loadProducts() {
        val res = ApiClient.instance.getProducts()
        if (res.isSuccessful) products = res.body() ?: emptyList()
    }

    LaunchedEffect(Unit) { loadProducts() }

    val filteredProducts = products.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            placeholder = { Text("Ürün Ara") },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        if (filteredProducts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ürün bulunamadı", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                product.name,
                                color = Color(0xFF4A3AFF),
                                fontWeight = FontWeight.Medium
                            )
                            Text("%.2f ₺".format(product.price), color = Color(0xFF6B6B6B))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { showEditDialog = product },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                                    contentPadding = PaddingValues(vertical = 0.dp)
                                ) {
                                    Text(
                                        "Düzenle",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            ApiClient.instance.deleteProduct(product.id)
                                            loadProducts()
                                        }
                                    },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF7B61FF)),
                                    contentPadding = PaddingValues(vertical = 0.dp)
                                ) {
                                    Text(
                                        "Sil",
                                        fontSize = 13.sp,
                                        color = Color(0xFF7B61FF),
                                        maxLines = 1
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    // 🔹 Ürün düzenleme diyalogu (Helva destekli)
    showEditDialog?.let { product ->
        val isHelva = product.category?.lowercase()?.contains("helva") == true
        var newName by remember { mutableStateOf(product.name) }
        var newPrice by remember { mutableStateOf(product.price.toString()) }

        // Helva varyant fiyatları
        var small by remember { mutableStateOf("") }
        var large by remember { mutableStateOf("") }
        var smallIce by remember { mutableStateOf("") }
        var largeIce by remember { mutableStateOf("") }

        // Eğer helva varyantı varsa doldur
        if (isHelva && product.variants != null) {
            val variants = product.variants as? List<Map<String, Any>> ?: emptyList()
            small = variants.find { it["name"] == "Küçük" }?.get("price")?.toString() ?: ""
            large = variants.find { it["name"] == "Büyük" }?.get("price")?.toString() ?: ""
            smallIce =
                variants.find { it["name"] == "Küçük Dondurmalı" }?.get("price")?.toString() ?: ""
            largeIce =
                variants.find { it["name"] == "Büyük Dondurmalı" }?.get("price")?.toString() ?: ""
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Ürünü Düzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Ürün Adı") }
                    )

                    if (isHelva) {
                        OutlinedTextField(
                            value = small,
                            onValueChange = { small = it },
                            label = { Text("Küçük Boy") })
                        OutlinedTextField(
                            value = large,
                            onValueChange = { large = it },
                            label = { Text("Büyük Boy") })
                        OutlinedTextField(
                            value = smallIce,
                            onValueChange = { smallIce = it },
                            label = { Text("Küçük Dondurmalı") })
                        OutlinedTextField(
                            value = largeIce,
                            onValueChange = { largeIce = it },
                            label = { Text("Büyük Dondurmalı") })
                    } else {
                        OutlinedTextField(
                            value = newPrice,
                            onValueChange = { newPrice = it },
                            label = { Text("Fiyat") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val body = if (isHelva) {
                            hashMapOf<String, Any>(
                                "name" to newName,
                                "price" to 0.0,
                                "variants" to listOf(
                                    hashMapOf(
                                        "name" to "Küçük",
                                        "price" to (small.toDoubleOrNull() ?: 0.0)
                                    ),
                                    hashMapOf(
                                        "name" to "Büyük",
                                        "price" to (large.toDoubleOrNull() ?: 0.0)
                                    ),
                                    hashMapOf(
                                        "name" to "Küçük Dondurmalı",
                                        "price" to (smallIce.toDoubleOrNull() ?: 0.0)
                                    ),
                                    hashMapOf(
                                        "name" to "Büyük Dondurmalı",
                                        "price" to (largeIce.toDoubleOrNull() ?: 0.0)
                                    )
                                )
                            )
                        } else {
                            hashMapOf<String, Any>(
                                "name" to newName,
                                "price" to (newPrice.toDoubleOrNull() ?: 0.0)
                            )
                        }

                        ApiClient.instance.updateProduct(product.id, HashMap(body))
                        showEditDialog = null
                        loadProducts()
                    }
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) { Text("İptal") }
            }
        )
    }
}

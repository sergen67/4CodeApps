package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Column(modifier = Modifier.fillMaxSize()) {
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

    // 🔹 Kategorileri yükle
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

            // 🔹 Helva kategorisi için özel form
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
                                "price" to 0.0, // 🔹 Helvalar ana fiyat sıfır
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
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Helva Ürününü Kaydet") }

            } else {
                // 🔹 Normal ürün formu
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ürün Adı") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Fiyat") })

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
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ürünü Kaydet") }
            }
        }
    }
}
/* 🔹 NORMAL ÜRÜN KARTI */
@Composable
fun NormalProductCard(product: Product) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF4A3AFF))
            Spacer(Modifier.height(4.dp))
            Text("%.2f ₺".format(product.price), color = Color(0xFF6B6B6B), fontSize = 14.sp)
        }
    }
}

/* 🔹 HELVA ÜRÜN KARTI (VARYANTLARLA) */
@Composable
fun ProductListTab(modifier: Modifier = Modifier,vm: MainViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

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
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
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
                    // 🔹 Helva mı kontrol et
                    if (product.category?.lowercase()?.contains("helva") == true) {
                        HelvaProductCard(product,vm)
                    } else {
                        NormalProductCard(product)
                    }

                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelvaProductCard(product: Product, vm: MainViewModel) {
    var showSheet by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E7)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { showSheet = true } // 🔹 Tıklayınca varyant menüsü açılır
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF9C6615))
            Text("Tıklayarak varyant seç", fontSize = 13.sp, color = Color.Gray)
        }
    }

    // 🔽 Alt menü (Modal Bottom Sheet)
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
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
                    text = "${product.name} Seçenekleri",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF6E4A00)
                )
                Divider(color = Color(0xFFE0C097))

                val variants = product.variants ?: emptyList()
                if (variants.isEmpty()) {
                    Text("Varyant bilgisi yok", color = Color.Gray)
                } else {
                    variants.forEach { variant ->
                        val vName = variant["name"]?.toString() ?: "-"
                        val vPrice = (variant["price"] as? Number)?.toDouble() ?: 0.0

                        Surface(
                            color = Color(0xFFFFEAB5),
                            shape = RoundedCornerShape(14.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // 🔹 Sepete ekle
                                    val selectedProduct = product.copy(
                                        name = "${product.name} - $vName",
                                        price = vPrice
                                    )
                                    vm.addToCart(selectedProduct)
                                    showSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(vName, fontSize = 15.sp, color = Color(0xFF5E3A00))
                                Text("%.2f ₺".format(vPrice), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C6615))
                ) {
                    Text("Kapat", color = Color.White)
                }
            }
        }
    }
}

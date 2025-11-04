package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.BorderStroke
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
        CenterAlignedTopAppBar(
            title = { Text("Ürün Yönetimi", fontWeight = FontWeight.SemiBold) }
        )
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
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Fiyat") },
                shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(16.dp),
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
            ) {
                Text("Ürünü Kaydet", color = Color.White)
            }
        }
    }
}

@Composable
fun ProductListTab(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var editProduct by remember { mutableStateOf<Product?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    suspend fun loadProducts() {
        val res = ApiClient.instance.getProducts()
        if (res.isSuccessful) products = res.body() ?: emptyList()
    }

    LaunchedEffect(Unit) { loadProducts() }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
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
                    ProductItemCard(
                        product = product,
                        onEdit = { editProduct = product },
                        onDelete = {
                            scope.launch {
                                ApiClient.instance.deleteProduct(product.id)
                                loadProducts()
                            }
                        }
                    )
                }
            }
        }
    }

    editProduct?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = { editProduct = null },
            onSave = {
                scope.launch {
                    loadProducts()
                    editProduct = null
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF4A3AFF))
                Spacer(Modifier.height(4.dp))
                Text("%.2f ₺".format(product.price), color = Color(0xFF6B6B6B), fontSize = 14.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) { Text("Düzenle", color = Color.White) }

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color(0xFF7B61FF)),
                    modifier = Modifier.weight(1f)
                ) { Text("Sil", color = Color(0xFF7B61FF)) }
            }
        }
    }
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var newName by remember { mutableStateOf(product.name) }
    var newPrice by remember { mutableStateOf(product.price.toString()) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ürünü Düzenle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Ürün Adı") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text("Fiyat") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val body = hashMapOf<String, Any>(
                        "name" to newName,
                        "price" to (newPrice.toDoubleOrNull() ?: 0.0)
                    )
                    ApiClient.instance.updateProduct(product.id, HashMap(body))
                    onSave()
                }
            }) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

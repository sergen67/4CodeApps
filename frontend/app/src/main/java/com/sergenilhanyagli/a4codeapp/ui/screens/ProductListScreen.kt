package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.Product
import com.sergenilhanyagli.a4codeapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    nav: NavHostController,
    vm: MainViewModel
) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var refreshKey by remember { mutableStateOf(0) } // ✅ admin ekleme sonrası tetikleme
    var categories by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) { // ✅ refresh tetikleyici
        isLoading = true
        try {
            val resProducts = ApiClient.instance.getProducts()
            val resCategories = ApiClient.instance.getCategories()
            if (resProducts.isSuccessful) products = resProducts.body() ?: emptyList()
            if (resCategories.isSuccessful) categories = resCategories.body() ?: emptyList()
        } finally { isLoading = false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Ürün Listesi") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Sepet (${vm.cartItems.size})") },
                onClick = { nav.navigate("cart") },
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Button(
                onClick = { refreshKey++ }, // ✅ manuel yenileme de eklendi
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ürünleri Yenile")
            }
            Spacer(Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(products) { product ->
                        ProductCard(product = product) { vm.addToCart(product) }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!product.imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${product.price} ₺",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
            )
            if (!product.category.isNullOrEmpty()) {
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Sepete Ekle", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

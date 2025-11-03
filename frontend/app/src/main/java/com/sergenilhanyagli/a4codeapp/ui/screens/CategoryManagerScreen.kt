package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerScreen(nav: NavHostController) {
    var categories by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var newCategory by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun loadCategories() {
        scope.launch {
            val res = ApiClient.instance.getCategories()
            if (res.isSuccessful) categories = res.body() ?: emptyList()
        }
    }

    LaunchedEffect(Unit) { loadCategories() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kategori Yönetimi", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = newCategory,
            onValueChange = { newCategory = it },
            label = { Text("Yeni Kategori Adı") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (newCategory.isNotBlank()) {
                    scope.launch {
                        ApiClient.instance.createCategory(
                            hashMapOf("name" to newCategory)
                        )
                        newCategory = ""
                        loadCategories()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Kategori Ekle") }

        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${cat["name"]}")
                        Text(
                            "Ürün sayısı: ${((cat["_count"] as? Map<*, *>)?.get("products") ?: 0)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

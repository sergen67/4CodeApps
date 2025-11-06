package com.sergenilhanyagli.a4codeapp.ui.screens

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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerScreen(nav: NavHostController) {
    var categories by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var newCategory by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            val res = ApiClient.instance.getCategories()
            if (res.isSuccessful) categories = res.body() ?: emptyList()
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        containerColor = Color(0xFFF6F3FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kategori Yönetimi", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF7B61FF))
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = newCategory,
                onValueChange = { newCategory = it },
                label = { Text("Yeni Kategori Adı") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (newCategory.isNotBlank()) {
                        scope.launch {
                            ApiClient.instance.createCategory(hashMapOf("name" to newCategory))
                            newCategory = ""
                            load()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ekle", color = Color.White) }

            Divider(color = Color(0xFFDDD4FF))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories) { cat ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(cat["name"].toString(), fontWeight = FontWeight.Bold, color = Color(0xFF4A3AFF))
                            Text(
                                "Ürün sayısı: ${((cat["_count"] as? Map<*, *>)?.get("products") ?: 0)}",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

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
import com.sergenilhanyagli.a4codeapp.data.models.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagerScreen(nav: NavHostController) {
    var employees by remember { mutableStateOf<List<User>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    suspend fun loadEmployees() {
        val res = ApiClient.instance.getUsers()
        if (res.isSuccessful) employees = res.body() ?: emptyList()
    }

    LaunchedEffect(Unit) { loadEmployees() }

    Scaffold(
        containerColor = Color(0xFFF6F3FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Çalışan Yönetimi", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF7B61FF))
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Yeni Çalışan Ekle", fontWeight = FontWeight.Bold, color = Color(0xFF4A3AFF))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ad Soyad") })
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Şifre") })

            Button(
                onClick = {
                    scope.launch {
                        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                            val body = hashMapOf<String, Any>(
                                "name" to name,
                                "email" to email,
                                "password" to password,
                                "role" to "user"
                            )
                            ApiClient.instance.register(body)
                            name = ""; email = ""; password = ""
                            loadEmployees()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ekle", color = Color.White) }

            Divider(color = Color(0xFFDDD4FF))

            Text("Çalışan Listesi", fontWeight = FontWeight.Bold, color = Color(0xFF4A3AFF))

            if (employees.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kayıtlı çalışan yok", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(employees) { user ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(user.name, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A3AFF))
                                Text(user.email, color = Color.Gray)
                                Text("Rol: ${user.role}", color = Color(0xFF6B6B6B))
                            }
                        }
                    }
                }
            }
        }
    }
}

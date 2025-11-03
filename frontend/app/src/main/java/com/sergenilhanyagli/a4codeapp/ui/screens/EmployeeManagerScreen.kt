package com.sergenilhanyagli.a4codeapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.User
import kotlinx.coroutines.launch

@Composable
fun EmployeeManagerScreen(nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var employees by remember { mutableStateOf<List<User>>(emptyList()) }
    var result by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // 🔹 Tüm kullanıcıları çek
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.instance.getUsers()
            if (res.isSuccessful) {
                employees = res.body()?.filter { it.role == "employee" } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("4CODEAPP", "Çalışan listesi alınamadı: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Yeni Çalışan Oluştur", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        TextField(value = name, onValueChange = { name = it }, label = { Text("Ad Soyad") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        TextField(value = password, onValueChange = { password = it }, label = { Text("Şifre") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            scope.launch {
                try {
                    val user = hashMapOf<String, Any>(
                        "name" to name,
                        "email" to email,
                        "password" to password,
                        "role" to "employee"
                    )
                    val res = ApiClient.instance.register(user)
                    if (res.isSuccessful) {
                        result = "✅ Çalışan oluşturuldu"
                        val updated = ApiClient.instance.getUsers()
                        if (updated.isSuccessful)
                            employees = updated.body()?.filter { it.role == "employee" } ?: emptyList()
                    } else {
                        result = "❌ Hata: ${res.code()}"
                    }
                } catch (e: Exception) {
                    result = "❌ Hata: ${e.message}"
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Kaydet")
        }

        Spacer(Modifier.height(16.dp))
        if (result.isNotEmpty()) Text(result, color = MaterialTheme.colorScheme.primary)

        Divider(Modifier.padding(vertical = 16.dp))

        Text("Çalışanlar", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(employees) { emp ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(emp.name)
                        Text(emp.email, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

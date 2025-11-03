package com.sergenilhanyagli.a4codeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sergenilhanyagli.a4codeapp.data.models.User

@Composable
fun LoginScreen(
    nav: NavHostController,
    vm: MainViewModel = viewModel(),
    onLoginSuccess: (User) -> Unit = {} // 🔹 MainActivity’den kullanıcıyı geri almak için
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Giriş Yap", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                scope.launch {
                    try {
                        val success = vm.login(email, password)
                        if (success && vm.user != null) {
                            val user = vm.user!!
                            onLoginSuccess(user) // 🔹 Kullanıcıyı MainActivity’ye gönder
                            if (user.role == "admin") {
                                nav.navigate("admin")
                            } else {
                                nav.navigate("products")
                            }
                        } else {
                            message = "Geçersiz email veya şifre"
                        }
                    } catch (e: Exception) {
                        message = "Hata: ${e.message}"
                    }
                }
            }) {
                Text("Giriş Yap")
            }

            if (message.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { nav.navigate("register") }) {
                Text("Hesabın yok mu? Kayıt ol")
            }
        }
    }
}

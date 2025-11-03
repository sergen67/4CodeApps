package com.sergenilhanyagli.a4codeapp.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.User
import kotlinx.coroutines.launch
import retrofit2.Response

private val Response<User>.isSuccessful: Boolean
    get() {
        TODO()
    }

@Composable
fun RegisterScreen(nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Kayıt Ol", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            TextField(value = name, onValueChange = { name = it }, label = { Text("İsim") })
            Spacer(Modifier.height(8.dp))
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Spacer(Modifier.height(8.dp))
            TextField(value = password, onValueChange = { password = it }, label = { Text("Şifre") })
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                scope.launch {
                    val res = ApiClient.instance.register(User(name = name, email = email, password = password))
                    if (res.isSuccessful) nav.navigate("login")
                }
            }) { Text("Kayıt Ol") }
        }
    }
}

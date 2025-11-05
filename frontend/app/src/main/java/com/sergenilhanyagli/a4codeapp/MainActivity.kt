package com.sergenilhanyagli.a4codeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sergenilhanyagli.a4codeapp.data.models.User
import com.sergenilhanyagli.a4codeapp.ui.screens.*
import com.sergenilhanyagli.a4codeapp.ui.theme._4CodeAppTheme
import com.sergenilhanyagli.a4codeapp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            _4CodeAppTheme {
                MainActivityContent()
            }
        }
    }
}

@Composable
fun MainActivityContent() {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(navController = nav, startDestination = "login") {

        // 🔹 Giriş ekranı
        composable("login") {
            LoginScreen(nav) { loggedInUser ->
                currentUser = loggedInUser
                if (loggedInUser.role == "admin") {
                    nav.navigate("admin")
                } else {
                    nav.navigate("products")
                }
            }
        }

        composable("register") { RegisterScreen(nav) }

        // 🔹 Ürün listesi (çalışan tarafı)
        composable("products") {
            ProductListScreen(nav,vm)
        }

        // 🔹 Sepet ve ödeme ekranı (parametre: toplam fiyat)
        composable("cart") {
            // ✅ CartScreen parametreleri: nav, currentUser, vm
            currentUser?.let { user ->
                CartScreen(nav, user, vm)
            } ?: run {
                // Kullanıcı boşsa girişe yönlendir
                nav.navigate("login")
            }
        }


        composable("orders") { OrderListScreen(nav) }

        composable("admin") { AdminScreen(nav) }
    }
}

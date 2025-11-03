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
    val vm: MainViewModel = viewModel() // ortak ViewModel
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(navController = nav, startDestination = "login") {

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
        composable("products") { ProductListScreen(nav, vm) } // 🔹 vm geçiyoruz

        composable("cart") {
            currentUser?.let { user ->
                CartScreen(nav, user, vm) // 🔹 aynı vm
            }
        }

        composable("orders") { OrderListScreen(nav) }
        composable("admin") { AdminScreen(nav) }
    }
}


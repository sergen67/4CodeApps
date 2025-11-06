package com.sergenilhanyagli.a4codeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    NavHost(navController = nav, startDestination = "login") {

        composable("login") {
            LoginScreen(nav) { loggedInUser ->
                vm.user = loggedInUser
                val destination = if (loggedInUser.role == "admin") "admin" else "products"
                nav.navigate(destination) {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("products") { ProductListScreen(nav, vm) }
        composable("cart") {
            vm.user?.let { user ->
                CartScreen(nav, user, vm)
            }
        }
        composable("orders") { OrderListScreen(nav) }
        composable("admin") { AdminScreen(nav) }
    }
}

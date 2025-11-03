package com.sergenilhanyagli.a4codeapp.viewmodel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.LoginRequest
import com.sergenilhanyagli.a4codeapp.data.models.Product
import com.sergenilhanyagli.a4codeapp.data.models.User


class MainViewModel : ViewModel() {
    var products = mutableStateListOf<Product>()
    var user by mutableStateOf<User?>(null)
    var cart = mutableStateListOf<Product>()

    suspend fun loadProducts() {
        val res = ApiClient.instance.getProducts()
        if (res.isSuccessful) {
            products.clear()
            products.addAll(res.body() ?: emptyList())
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        val res = ApiClient.instance.login(LoginRequest(email, password))
        return if (res.isSuccessful) {
            user = res.body()
            true
        } else false
    }

    // 🔹 Sepetteki ürünler
    var cartItems = mutableStateListOf<Product>()
        private set

    // 🔹 Sepete ürün ekle
    fun addToCart(product: Product) {
        cartItems.add(product)
    }

    // 🔹 Sepeti temizle
    fun clearCart() {
        cartItems.clear()
    }

}

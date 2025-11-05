package com.sergenilhanyagli.a4codeapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sergenilhanyagli.a4codeapp.data.ApiClient
import com.sergenilhanyagli.a4codeapp.data.models.CartItem
import com.sergenilhanyagli.a4codeapp.data.models.LoginRequest
import com.sergenilhanyagli.a4codeapp.data.models.Product
import com.sergenilhanyagli.a4codeapp.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    var products = mutableStateListOf<Product>()
    var user by mutableStateOf<User?>(null)
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

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

    // 🔹 Ürün sepete ekle (aynıysa miktar +1)
    fun addToCart(product: Product) {
        val index = _cartItems.indexOfFirst { it.product.name == product.name }
        if (index != -1) {
            val old = _cartItems[index]
            _cartItems[index] = old.copy(quantity = old.quantity + 1)
        } else {
            _cartItems.add(CartItem(product, 1))
        }
    }

    // 🔹 Ürün azalt veya sil
    fun removeFromCart(product: Product) {
        val index = _cartItems.indexOfFirst { it.product.name == product.name }
        if (index != -1) {
            val old = _cartItems[index]
            if (old.quantity > 1)
                _cartItems[index] = old.copy(quantity = old.quantity - 1)
            else
                _cartItems.removeAt(index)
        }
    }

    // 🔹 Sepeti tamamen temizle
    fun clearCart() {
        _cartItems.clear()
    }

    // 🔹 Toplam tutar
    fun totalPrice(): Double = _cartItems.sumOf { it.product.price * it.quantity }

    suspend fun completeSale(paymentType: String): Boolean {
        return try {
            val currentUser = user ?: return false
            val total = totalPrice()

            println("🧾 SATIŞ BAŞLATILIYOR")
            println("➡️ userId=${currentUser.id}, totalPrice=$total, paymentType=$paymentType")

            val body = hashMapOf<String, Any>(
                "userId" to (currentUser.id ?: 0),
                "totalPrice" to total,
                "paymentType" to paymentType
            )

            val res = ApiClient.instance.createSale(HashMap(body))
            println("⬅️ Yanıt kodu: ${res.code()}, başarı: ${res.isSuccessful}")

            if (res.isSuccessful) {
                println("✅ Satış tamamlandı!")
                clearCart()
                true
            } else {
                println("❌ Sunucu hatası: ${res.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            println("🚨 HATA (completeSale): ${e.message}")
            false
        }
    }



}

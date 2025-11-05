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


class MainViewModel : ViewModel() {
    var products = mutableStateListOf<Product>()
    var user by mutableStateOf<User?>(null)
    var cart = mutableStateListOf<Product>()
    val cartItems: List<CartItem> get() = _cartItems
    private val _cartItems = mutableStateListOf<CartItem>()
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

    // 🔹 Sepete ürün ekleme (aynı ürün varsa miktar artar)
    fun addToCart(product: Product) {
        val index = _cartItems.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val oldItem = _cartItems[index]
            val newItem = oldItem.copy(quantity = oldItem.quantity + 1)
            _cartItems[index] = newItem // ⚡ listeye yeniden atıyoruz ki Compose güncellesin
        } else {
            _cartItems.add(CartItem(product, 1))
        }
    }

    // 🔹 Ürün azaltma / silme
    fun removeFromCart(product: Product) {
        val index = _cartItems.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val oldItem = _cartItems[index]
            if (oldItem.quantity > 1) {
                _cartItems[index] = oldItem.copy(quantity = oldItem.quantity - 1)
            } else {
                _cartItems.removeAt(index)
            }
        }
    }

    fun clearCart() {
        _cartItems.clear()
    }

    fun totalPrice(): Double = _cartItems.sumOf { it.product.price * it.quantity }
}

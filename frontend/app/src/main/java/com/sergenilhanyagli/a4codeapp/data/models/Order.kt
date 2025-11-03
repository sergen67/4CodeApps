package com.sergenilhanyagli.a4codeapp.data.models
data class Order(
    val id: Int? = null,
    val userId: Int,
    val items: List<Product>,
    val totalPrice: Double
)

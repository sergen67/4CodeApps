package com.sergenilhanyagli.a4codeapp.data.models
data class Product(
    val id: Int? = null,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val category: String,
    val categoryId: Int?
)

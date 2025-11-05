package com.sergenilhanyagli.a4codeapp.data.models

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val categoryId: Int?,
    val category: String? = null,
    val variants: List<Map<String, Any>>? = null
)

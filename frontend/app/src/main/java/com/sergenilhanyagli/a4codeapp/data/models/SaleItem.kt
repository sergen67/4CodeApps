package com.sergenilhanyagli.a4codeapp.data.models

data class SaleItem(
    val id: Int,
    val saleId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val price: Double
)

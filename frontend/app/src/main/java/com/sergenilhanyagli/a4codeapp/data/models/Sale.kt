package com.sergenilhanyagli.a4codeapp.data.models

data class Sale(
    val id: Int,
    val userId: Int,
    val user: User?,
    val totalPrice: Double,
    val paymentType: String,
    val createdAt: String,
    val items: List<SaleItem>
)

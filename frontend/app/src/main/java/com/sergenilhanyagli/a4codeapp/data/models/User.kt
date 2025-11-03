package com.sergenilhanyagli.a4codeapp.data.models
data class User(
    val id: Int? = null,
    val name: String,
    val email: String,
    val password: String,
    val role: String = "user"   // 🔹 eklendi
)

package com.example.marikitiapp.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val quantity: Int = 0,
    val category: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

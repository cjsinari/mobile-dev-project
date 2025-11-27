package com.example.marikitiapp.ui.sharon

import java.util.Date

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val originalPrice: Double? = null,
    val description: String,
    val imageUrl: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val inStock: Boolean = true,
    val sellerId: String = ""
//    @DrawableRes val imageRes: Int,
)

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPrice: Double
        get() = product.price * quantity
}

data class PaymentMethod(
    val id: String,
    val name: String,
    val icon: String,
    val isSelected: Boolean = false,
    val description: String
)

data class Order(
    val id: String,
    val buyerId: String,
    val paymentMethod: String, // "mpesa" or "cash"
    val paymentStatus: String, // "pending", "complete", "cancelled"
    val deliveryStatus: String, // "pending", "delivered"
    val totalAmount: Double,
    val timestamp: Date
)


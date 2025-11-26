package com.example.marikitiapp

import com.google.firebase.Timestamp

/**
 * Data class representing a product in the Firestore database.
 * The empty default values are required for Firebase's toObjects() method.
 */
data class Product(
    // Fields from your desired class, now with default values
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0, // Changed from Any to Double
    val originalPrice: Double? = null,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val sellerId: String = "",
    val inStock: Boolean = true,
    val imageUrl: String = "",
    val timestamp: Timestamp? = null, // Using the correct Firebase Timestamp
    val imageRes: Int = 0, // Added default value
    val brand: String = "", // Added default value
    val category: String = ""
)

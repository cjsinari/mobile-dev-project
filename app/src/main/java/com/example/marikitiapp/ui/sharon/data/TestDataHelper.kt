package com.example.marikitiapp.ui.sharon.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Helper class to create test data in Firestore
 * Call this once to populate your database with sample products
 */
object TestDataHelper {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    /**
     * Creates sample products in Firestore for testing
     * Call this method once after setting up Firebase
     */
    suspend fun createTestProducts() {
        // --- CHANGES ---
        // 1. Prices adjusted to reflect Kenyan Shillings (Ksh).
        // 2. Dummy image URLs added from a placeholder service.
        val testProducts = listOf(
            hashMapOf(
                "name" to "Premium Wireless Headphones",
                "description" to "Experience premium sound quality with our wireless headphones. Features noise cancellation, 30-hour battery life, and comfortable over-ear design.",
                "price" to 12500.00, // Price in Ksh
                "originalPrice" to 15000.00, // Price in Ksh
                "imageUrl" to "https://picsum.photos/seed/headphones/400/300", // Dummy Image URL
                "sellerId" to "seller_001",
                "rating" to 4.5,
                "reviewCount" to 234L,
                "inStock" to true,
                "timestamp" to com.google.firebase.Timestamp.now()
            ),
            hashMapOf(
                "name" to "Smart Watch Pro",
                "description" to "Track your fitness and stay connected with our smart watch. Features heart rate monitoring, GPS, and 7-day battery life.",
                "price" to 8999.99, // Price in Ksh
                "originalPrice" to null,
                "imageUrl" to "https://picsum.photos/seed/smartwatch/400/300", // Dummy Image URL
                "sellerId" to "seller_002",
                "rating" to 4.8,
                "reviewCount" to 156L,
                "inStock" to true,
                "timestamp" to com.google.firebase.Timestamp.now()
            ),
            hashMapOf(
                "name" to "Wireless Earbuds",
                "description" to "Compact wireless earbuds with crystal clear sound and noise cancellation. Perfect for workouts and daily use.",
                "price" to 4500.00, // Price in Ksh
                "originalPrice" to 5500.00, // Price in Ksh
                "imageUrl" to "https://picsum.photos/seed/earbuds/400/300", // Dummy Image URL
                "sellerId" to "seller_001",
                "rating" to 4.3,
                "reviewCount" to 89L,
                "inStock" to true,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
        )

        try {
            // To avoid creating duplicates, let's first check if products exist.
            if (productsCollection.limit(1).get().await().isEmpty) {
                testProducts.forEach { productData ->
                    val docRef = productsCollection.add(productData).await()
                    println("Created test product with ID: ${docRef.id}")
                }
                println("Test products created successfully!")
            } else {
                println("Products collection is not empty. Skipping test data creation.")
            }
        } catch (e: Exception) {
            println("Error creating test products: ${e.message}")
            // It's better not to re-throw the exception in a helper unless needed.
        }
    }
}

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
        val testProducts = listOf(
            hashMapOf(
                "name" to "Premium Wireless Headphones",
                "description" to "Experience premium sound quality with our wireless headphones. Features noise cancellation, 30-hour battery life, and comfortable over-ear design.",
                "price" to 199.99,
                "originalPrice" to 249.99,
                "imageUrl" to "",
                "sellerId" to "seller_001",
                "rating" to 4.5,
                "reviewCount" to 234L,
                "inStock" to true,
                "timestamp" to com.google.firebase.Timestamp.now()
            ),
            hashMapOf(
                "name" to "Smart Watch Pro",
                "description" to "Track your fitness and stay connected with our smart watch. Features heart rate monitoring, GPS, and 7-day battery life.",
                "price" to 299.99,
                "originalPrice" to null,
                "imageUrl" to "",
                "sellerId" to "seller_002",
                "rating" to 4.8,
                "reviewCount" to 156L,
                "inStock" to true,
                "timestamp" to com.google.firebase.Timestamp.now()
            ),
            hashMapOf(
                "name" to "Wireless Earbuds",
                "description" to "Compact wireless earbuds with crystal clear sound and noise cancellation. Perfect for workouts and daily use.",
                "price" to 79.99,
                "originalPrice" to 99.99,
                "imageUrl" to "",
                "sellerId" to "seller_001",
                "rating" to 4.3,
                "reviewCount" to 89L,
                "inStock" to true,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
        )

        try {
            testProducts.forEach { productData ->
                val docRef = productsCollection.add(productData).await()
                println("Created test product with ID: ${docRef.id}")
            }
            println("Test products created successfully!")
        } catch (e: Exception) {
            println("Error creating test products: ${e.message}")
            throw e
        }
    }
}


package com.example.marikitiapp.data.repository

import com.example.marikitiapp.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseProductRepository {
    private val db = FirebaseFirestore.getInstance()

     //Save a product to Firestore
     //Return the document ID of the saved product
    suspend fun saveProduct(product: Product): String {
        val productData = hashMapOf(
            "name" to product.name,
            "price" to product.price,
            "description" to product.description,
            "quantity" to product.quantity,
            "category" to product.category,
            "imageUrl" to product.imageUrl,
            "createdAt" to product.createdAt
        )

        val docRef = db.collection("products").add(productData).await()
        return docRef.id
    }

     //Get all products ordered by creation date (newest first)
     //For testing without authentication

    suspend fun getAllProducts(): List<Product> {
        val snapshot = db.collection("products")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                Product(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    quantity = doc.getLong("quantity")?.toInt() ?: 0,
                    category = doc.getString("category") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null // Skip malformed documents
            }
        }
    }


     //Get products by seller ID (for future use when auth is implemented)

    suspend fun getProductsBySeller(sellerId: String): List<Product> {
        val snapshot = db.collection("products")
            .whereEqualTo("sellerId", sellerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                Product(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    quantity = doc.getLong("quantity")?.toInt() ?: 0,
                    category = doc.getString("category") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }


    //Get a single product by ID

    suspend fun getProductById(productId: String): Product? {
        val doc = db.collection("products").document(productId).get().await()
        return if (doc.exists()) {
            Product(
                id = doc.id,
                name = doc.getString("name") ?: "",
                price = doc.getDouble("price") ?: 0.0,
                description = doc.getString("description") ?: "",
                quantity = doc.getLong("quantity")?.toInt() ?: 0,
                category = doc.getString("category") ?: "",
                imageUrl = doc.getString("imageUrl"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        } else null
    }


     //Update an existing product

    suspend fun updateProduct(productId: String, product: Product): Boolean {
        return try {
            val updates = hashMapOf(
                "name" to product.name,
                "price" to product.price,
                "description" to product.description,
                "quantity" to product.quantity,
                "category" to product.category,
                "imageUrl" to product.imageUrl
            )
            db.collection("products").document(productId).update(updates as Map<String, Any>).await()
            true
        } catch (e: Exception) {
            false
        }
    }


    //Delete a product

    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            db.collection("products").document(productId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }


    //Get products by category

    suspend fun getProductsByCategory(category: String): List<Product> {
        val snapshot = db.collection("products")
            .whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                Product(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    description = doc.getString("description") ?: "",
                    quantity = doc.getLong("quantity")?.toInt() ?: 0,
                    category = doc.getString("category") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
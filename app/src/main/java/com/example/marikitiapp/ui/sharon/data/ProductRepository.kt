package com.example.marikitiapp.ui.sharon.data

import com.example.marikitiapp.ui.sharon.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    /**
     * Get a single product by ID with live updates
     */
    fun getProduct(productId: String): Flow<Result<Product>> = callbackFlow {
        val listener = productsCollection.document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val product = snapshot.toProduct()
                        trySend(Result.success(product))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                } else {
                    trySend(Result.failure(Exception("Product not found")))
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get all products with live updates
     */
    fun getAllProducts(): Flow<Result<List<Product>>> = callbackFlow {
        val listener = productsCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val products = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toProduct()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(Result.success(products))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                } else {
                    trySend(Result.success(emptyList()))
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Create a new product
     */
    suspend fun createProduct(product: Product): Result<String> = try {
        val productData = mutableMapOf<String, Any>(
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "sellerId" to product.sellerId,
            "rating" to product.rating,
            "reviewCount" to product.reviewCount,
            "inStock" to product.inStock,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        
        // Add originalPrice only if it's not null
        product.originalPrice?.let {
            productData["originalPrice"] = it
        }

        val docRef = productsCollection.add(productData).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update an existing product
     */
    suspend fun updateProduct(product: Product): Result<Unit> = try {
        val productData = mutableMapOf<String, Any>(
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "rating" to product.rating,
            "reviewCount" to product.reviewCount,
            "inStock" to product.inStock
        )
        
        // Add originalPrice only if it's not null
        product.originalPrice?.let {
            productData["originalPrice"] = it
        }

        productsCollection.document(product.id).update(productData).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toProduct(): Product {
        return Product(
            id = id,
            name = getString("name") ?: "",
            description = getString("description") ?: "",
            price = getDouble("price") ?: 0.0,
            originalPrice = getDouble("originalPrice"),
            imageUrl = getString("imageUrl") ?: "",
            rating = (getDouble("rating") ?: 0.0).toFloat(),
            reviewCount = (getLong("reviewCount") ?: 0L).toInt(),
            inStock = getBoolean("inStock") ?: true,
            sellerId = getString("sellerId") ?: ""
        )
    }
}


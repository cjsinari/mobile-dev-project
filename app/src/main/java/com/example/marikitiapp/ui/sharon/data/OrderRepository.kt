package com.example.marikitiapp.ui.sharon.data

import com.example.marikitiapp.ui.sharon.CartItem
import com.example.marikitiapp.ui.sharon.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    /**
     * Create a new order from cart items
     */
    suspend fun createOrder(
        cartItems: List<CartItem>,
        buyerId: String,
        paymentMethod: String
    ): Result<String> = try {
        val orderData = hashMapOf(
            "buyerId" to buyerId,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to "pending",
            "deliveryStatus" to "pending",
            "items" to cartItems.map { item ->
                hashMapOf(
                    "productId" to item.product.id,
                    "productName" to item.product.name,
                    "price" to item.product.price,
                    "quantity" to item.quantity
                )
            },
            "totalAmount" to cartItems.sumOf { it.totalPrice },
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        val docRef = ordersCollection.add(orderData).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get order by ID with live updates
     */
    fun getOrder(orderId: String): Flow<Result<Order>> = callbackFlow {
        val listener = ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val order = snapshot.toOrder()
                        trySend(Result.success(order))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                } else {
                    trySend(Result.failure(Exception("Order not found")))
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get all orders for a buyer
     */
    fun getBuyerOrders(buyerId: String): Flow<Result<List<Order>>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("buyerId", buyerId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val orders = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toOrder()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(Result.success(orders))
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
     * Update order payment status
     */
    suspend fun updatePaymentStatus(orderId: String, status: String): Result<Unit> = try {
        ordersCollection.document(orderId)
            .update("paymentStatus", status)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update order delivery status (triggers escrow release)
     */
    suspend fun updateDeliveryStatus(orderId: String, status: String): Result<Unit> = try {
        val updates = hashMapOf<String, Any>(
            "deliveryStatus" to status
        )
        
        // If delivered, automatically release payment (complete escrow)
        if (status == "delivered") {
            updates["paymentStatus"] = "complete"
        }
        
        ordersCollection.document(orderId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toOrder(): Order {
        return Order(
            id = id,
            buyerId = getString("buyerId") ?: "",
            paymentMethod = getString("paymentMethod") ?: "",
            paymentStatus = getString("paymentStatus") ?: "pending",
            deliveryStatus = getString("deliveryStatus") ?: "pending",
            totalAmount = getDouble("totalAmount") ?: 0.0,
            timestamp = getTimestamp("timestamp")?.toDate() ?: java.util.Date()
        )
    }
}


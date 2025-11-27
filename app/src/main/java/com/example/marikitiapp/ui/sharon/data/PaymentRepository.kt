package com.example.marikitiapp.ui.sharon.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val paymentsCollection = db.collection("payments")
    private val mpesaApiService = MpesaApiService()

    /**
     * Create a payment record (M-Pesa or Cash)
     */
    suspend fun createPayment(
        orderId: String,
        method: String, // "mpesa" or "cash"
        amount: Double,
        phoneNumber: String? = null // Required for M-Pesa
    ): Result<String> = try {
        val paymentData = hashMapOf(
            "orderId" to orderId,
            "method" to method,
            "amount" to amount,
            "status" to "pending",
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        // Add phone number for M-Pesa
        if (method == "mpesa" && phoneNumber != null) {
            paymentData["phoneNumber"] = phoneNumber
        }

        val docRef = paymentsCollection.add(paymentData).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Process M-Pesa STK Push payment
     * This calls your backend API which initiates the STK Push
     */
    suspend fun processMpesaPayment(
        phoneNumber: String,
        amount: Double,
        orderId: String
    ): Result<String> = try {
        // Step 1: Create payment record in Firestore first
        val paymentResult = createPayment(orderId, "mpesa", amount, phoneNumber)
        
        if (paymentResult.isFailure) {
            return paymentResult
        }
        
        val paymentId = paymentResult.getOrNull() 
            ?: return Result.failure(Exception("Failed to create payment record"))
        
        // Step 2: Initiate STK Push via backend API
        val stkPushRequest = StkPushRequest(
            phoneNumber = phoneNumber,
            amount = amount,
            orderId = orderId,
            paymentId = paymentId
        )
        
        val stkPushResult = mpesaApiService.initiateStkPush(stkPushRequest)
        
        if (stkPushResult.isFailure) {
            // Update payment status to failed
            updatePaymentStatus(paymentId, "failed").getOrNull()
            return Result.failure(
                stkPushResult.exceptionOrNull() 
                    ?: Exception("Failed to initiate STK Push")
            )
        }
        
        val stkResponse = stkPushResult.getOrNull() 
            ?: return Result.failure(Exception("No STK response"))
        
        // Step 3: Store checkout request ID in payment record
        paymentsCollection.document(paymentId).update(
            hashMapOf(
                "checkoutRequestID" to (stkResponse.checkoutRequestID ?: ""),
                "mpesaMessage" to (stkResponse.message ?: "")
            )
        ).await()
        
        // Payment is now pending - backend webhook will update status when user confirms
        // The user will receive STK Push on their phone
        Result.success(paymentId)
        
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Process Cash payment (marked as pending until delivery confirmation)
     */
    suspend fun processCashPayment(
        orderId: String,
        amount: Double
    ): Result<String> = try {
        val paymentResult = createPayment(orderId, "cash", amount)
        
        if (paymentResult.isSuccess) {
            // Cash payments are always pending until delivery
            updatePaymentStatus(paymentResult.getOrNull() ?: "", "pending")
            Result.success(paymentResult.getOrNull() ?: "")
        } else {
            Result.failure(paymentResult.exceptionOrNull() ?: Exception("Payment failed"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update payment status
     */
    suspend fun updatePaymentStatus(paymentId: String, status: String): Result<Unit> = try {
        paymentsCollection.document(paymentId)
            .update("status", status)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Cancel/Refund payment
     */
    suspend fun cancelPayment(paymentId: String): Result<Unit> = try {
        paymentsCollection.document(paymentId)
            .update("status", "refunded")
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}


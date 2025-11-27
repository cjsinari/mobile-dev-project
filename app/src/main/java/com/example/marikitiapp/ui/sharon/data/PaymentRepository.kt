package com.example.marikitiapp.ui.sharon.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val paymentsCollection = db.collection("payments")
    private val mpesaApiService = MpesaApiService()

    //create a payment record (M-Pesa or Cash)
    suspend fun createPayment(
        orderId: String,
        method: String,
        amount: Double,
        phoneNumber: String? = null
    ): Result<String> {
        return try {
            val paymentData = hashMapOf<String, Any>(
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
    }

    /**
     * Process M-Pesa STK Push payment
     * This calls the backend API which initiates the STK Push
     */

    suspend fun processMpesaPayment(
        phoneNumber: String,
        amount: Double,
        orderId: String
    ): Result<String> {

        //Creating payment record in Firestore first

        return try {
            val paymentResult = createPayment(orderId, "mpesa", amount, phoneNumber)

            if (paymentResult.isFailure) {
                return paymentResult
            }

            val paymentId = paymentResult.getOrNull()
                ?: return Result.failure(Exception("Failed to create payment record"))

            val stkPushRequest = StkPushRequest(
                phoneNumber = phoneNumber,
                amount = amount,
                orderId = orderId,
                paymentId = paymentId
            )

            val stkPushResult = mpesaApiService.initiateStkPush(stkPushRequest)

            if (stkPushResult.isFailure) {
                updatePaymentStatus(paymentId, "failed").getOrNull()
                return Result.failure(
                    stkPushResult.exceptionOrNull()
                        ?: Exception("Failed to initiate STK Push")
                )
            }

            val stkResponse = stkPushResult.getOrNull()
                ?: return Result.failure(Exception("No STK response"))

            paymentsCollection.document(paymentId).update(
                mapOf(
                    "checkoutRequestID" to (stkResponse.checkoutRequestID ?: ""),
                    "mpesaMessage" to (stkResponse.message ?: "")
                )
            ).await()

            Result.success(paymentId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processCashPayment(
        orderId: String,
        amount: Double
    ): Result<String> {
        return try {
            val paymentResult = createPayment(orderId, "cash", amount)

            if (paymentResult.isSuccess) {
                updatePaymentStatus(paymentResult.getOrNull() ?: "", "pending")
                Result.success(paymentResult.getOrNull() ?: "")
            } else {
                Result.failure(paymentResult.exceptionOrNull() ?: Exception("Payment failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePaymentStatus(paymentId: String, status: String): Result<Unit> {
        return try {
            paymentsCollection.document(paymentId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelPayment(paymentId: String): Result<Unit> {
        return try {
            paymentsCollection.document(paymentId)
                .update("status", "refunded")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
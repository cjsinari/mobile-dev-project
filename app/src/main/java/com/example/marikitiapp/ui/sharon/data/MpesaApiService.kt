package com.example.marikitiapp.ui.sharon.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * API Service for M-Pesa STK Push integration
 * 
 * IMPORTANT: Replace BACKEND_URL with your actual backend URL
 * Example: "https://your-backend.herokuapp.com" or "https://your-backend.railway.app"
 */
@Serializable
data class StkPushRequest(
    val phoneNumber: String,
    val amount: Double,
    val orderId: String,
    val paymentId: String
)

@Serializable
data class StkPushResponse(
    val success: Boolean,
    val checkoutRequestID: String? = null,
    val responseCode: String? = null,
    val message: String? = null,
    val paymentId: String? = null,
    val error: String? = null,
    val details: String? = null
)

class MpesaApiService {
    // TODO: Replace with your backend URL
    // For local testing: "http://10.0.2.2:3000" (Android emulator)
    // For deployed backend: "https://your-backend-url.com"
    private val baseUrl = "https://your-backend-url.com"
    
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }
    
    /**
     * Initiate M-Pesa STK Push payment
     * This calls your backend which then calls M-Pesa API
     */
    suspend fun initiateStkPush(request: StkPushRequest): Result<StkPushResponse> {
        return try {
            val response = client.post("$baseUrl/api/mpesa/stk-push") {
                contentType(ContentType.Application.Json)
                setBody(request)
                timeout {
                    requestTimeoutMillis = 30_000
                }
            }
            
            if (response.status.isSuccess()) {
                val result = response.body<StkPushResponse>()
                if (result.success) {
                    Result.success(result)
                } else {
                    Result.failure(Exception(result.error ?: "STK Push failed"))
                }
            } else {
                val errorBody = try {
                    response.body<StkPushResponse>()
                } catch (e: Exception) {
                    null
                }
                Result.failure(
                    Exception(
                        errorBody?.error ?: 
                        "HTTP ${response.status.value}: ${response.status.description}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
    
    /**
     * Check payment status (optional - for polling)
     * Your backend can implement this to check M-Pesa transaction status
     */
    suspend fun checkPaymentStatus(checkoutRequestID: String): Result<String> {
        return try {
            val response = client.get("$baseUrl/api/mpesa/status/$checkoutRequestID") {
                timeout {
                    requestTimeoutMillis = 10_000
                }
            }
            
            if (response.status.isSuccess()) {
                val status = response.body<String>()
                Result.success(status)
            } else {
                Result.failure(Exception("Failed to check payment status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


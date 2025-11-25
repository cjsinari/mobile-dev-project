# Real M-Pesa Integration Guide

## Overview

This guide will help you integrate real M-Pesa STK Push payments using Safaricom's Daraja API.

---

## Part 1: M-Pesa Developer Account Setup

### Step 1: Register for M-Pesa Developer Account

1. Go to https://developer.safaricom.co.ke/
2. Click **"Get API Credentials"** or **"Sign Up"**
3. Fill in your details:
   - Business name
   - Email address
   - Phone number
4. Verify your email
5. Complete the registration

### Step 2: Create an App

1. Log in to your developer account
2. Go to **"My Apps"**
3. Click **"Create App"**
4. Fill in:
   - App Name: `MarikitiApp` (or your choice)
   - Description: Your app description
5. Click **"Create"**

### Step 3: Get API Credentials

After creating the app, you'll get:
- **Consumer Key**: `your_consumer_key`
- **Consumer Secret**: `your_consumer_secret`

**Save these securely!**

### Step 4: Get Test Credentials (Sandbox)

For testing, you'll need:
- **Shortcode**: Test shortcode (usually `174379`)
- **Passkey**: Test passkey (provided in dashboard)
- **Test Phone Number**: Use `254708374149` for testing

### Step 5: Get Production Credentials (Later)

For production:
- Apply for production credentials
- Get your business shortcode
- Get production passkey
- Complete business verification

---

## Part 2: Backend Setup Options

You have two options:

### Option A: Use a Backend Service (Recommended for Quick Start)

Use services like:
- **Node.js + Express** (we'll create this)
- **Python + Flask/FastAPI**
- **Firebase Cloud Functions**
- **Third-party services** (Flutterwave, Pesapal)

### Option B: Use Firebase Cloud Functions

We'll create a Firebase Cloud Function to handle M-Pesa API calls.

---

## Part 3: Backend Implementation (Node.js Example)

### Step 1: Create Backend Project

```bash
mkdir mpesa-backend
cd mpesa-backend
npm init -y
npm install express axios dotenv cors
```

### Step 2: Create `.env` file

```env
MPESA_CONSUMER_KEY=your_consumer_key_here
MPESA_CONSUMER_SECRET=your_consumer_secret_here
MPESA_SHORTCODE=174379
MPESA_PASSKEY=your_passkey_here
MPESA_CALLBACK_URL=https://your-backend.com/api/mpesa/callback
MPESA_ENVIRONMENT=sandbox
```

### Step 3: Create `server.js`

```javascript
const express = require('express');
const axios = require('axios');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

// Get OAuth Token
async function getAccessToken() {
    const consumerKey = process.env.MPESA_CONSUMER_KEY;
    const consumerSecret = process.env.MPESA_CONSUMER_SECRET;
    const url = process.env.MPESA_ENVIRONMENT === 'sandbox' 
        ? 'https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials'
        : 'https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials';
    
    const auth = Buffer.from(`${consumerKey}:${consumerSecret}`).toString('base64');
    
    try {
        const response = await axios.get(url, {
            headers: {
                'Authorization': `Basic ${auth}`
            }
        });
        return response.data.access_token;
    } catch (error) {
        console.error('Error getting access token:', error);
        throw error;
    }
}

// Initiate STK Push
app.post('/api/mpesa/stk-push', async (req, res) => {
    try {
        const { phoneNumber, amount, orderId, paymentId } = req.body;
        
        // Validate input
        if (!phoneNumber || !amount || !orderId) {
            return res.status(400).json({ error: 'Missing required fields' });
        }
        
        // Get access token
        const accessToken = await getAccessToken();
        
        // Format phone number (remove + and ensure 254 format)
        const formattedPhone = phoneNumber.replace(/^\+/, '').replace(/^0/, '254');
        
        // Generate timestamp
        const timestamp = new Date().toISOString().replace(/[^0-9]/g, '').slice(0, -3);
        
        // Generate password
        const shortcode = process.env.MPESA_SHORTCODE;
        const passkey = process.env.MPESA_PASSKEY;
        const password = Buffer.from(`${shortcode}${passkey}${timestamp}`).toString('base64');
        
        // STK Push URL
        const stkPushUrl = process.env.MPESA_ENVIRONMENT === 'sandbox'
            ? 'https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest'
            : 'https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest';
        
        // Request payload
        const payload = {
            BusinessShortCode: shortcode,
            Password: password,
            Timestamp: timestamp,
            TransactionType: 'CustomerPayBillOnline',
            Amount: Math.round(amount),
            PartyA: formattedPhone,
            PartyB: shortcode,
            PhoneNumber: formattedPhone,
            CallBackURL: process.env.MPESA_CALLBACK_URL,
            AccountReference: orderId,
            TransactionDesc: `Payment for order ${orderId}`
        };
        
        // Make STK Push request
        const response = await axios.post(stkPushUrl, payload, {
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        res.json({
            success: true,
            checkoutRequestID: response.data.CheckoutRequestID,
            responseCode: response.data.ResponseCode,
            message: response.data.CustomerMessage,
            paymentId: paymentId
        });
        
    } catch (error) {
        console.error('STK Push error:', error.response?.data || error.message);
        res.status(500).json({ 
            error: 'Failed to initiate STK Push',
            details: error.response?.data || error.message
        });
    }
});

// M-Pesa Callback (Webhook)
app.post('/api/mpesa/callback', async (req, res) => {
    try {
        const callbackData = req.body;
        
        // Log the callback
        console.log('M-Pesa Callback:', JSON.stringify(callbackData, null, 2));
        
        // Extract data
        const body = callbackData.Body;
        const stkCallback = body?.stkCallback;
        
        if (stkCallback) {
            const resultCode = stkCallback.ResultCode;
            const resultDesc = stkCallback.ResultDesc;
            const checkoutRequestID = stkCallback.CheckoutRequestID;
            const callbackMetadata = stkCallback.CallbackMetadata;
            
            // Check if payment was successful
            if (resultCode === 0) {
                // Payment successful
                const items = callbackMetadata?.Item;
                const mpesaReceiptNumber = items?.find(item => item.Name === 'MpesaReceiptNumber')?.Value;
                const transactionDate = items?.find(item => item.Name === 'TransactionDate')?.Value;
                const phoneNumber = items?.find(item => item.Name === 'PhoneNumber')?.Value;
                
                // TODO: Update Firestore payment status
                // You'll need to:
                // 1. Find payment by checkoutRequestID or orderId
                // 2. Update status to "complete"
                // 3. Store mpesaReceiptNumber as transactionId
                
                console.log('Payment successful:', {
                    checkoutRequestID,
                    mpesaReceiptNumber,
                    transactionDate,
                    phoneNumber
                });
            } else {
                // Payment failed
                console.log('Payment failed:', {
                    checkoutRequestID,
                    resultCode,
                    resultDesc
                });
                
                // TODO: Update Firestore payment status to "failed"
            }
        }
        
        // Always respond to M-Pesa
        res.json({ ResultCode: 0, ResultDesc: 'Callback received' });
        
    } catch (error) {
        console.error('Callback error:', error);
        res.status(500).json({ ResultCode: 1, ResultDesc: 'Error processing callback' });
    }
});

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'ok' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`M-Pesa backend server running on port ${PORT}`);
});
```

### Step 4: Deploy Backend

Options:
- **Heroku**: Free tier available
- **Railway**: Easy deployment
- **Firebase Cloud Functions**: Integrated with your Firebase project
- **AWS Lambda**: Serverless
- **Your own server**: VPS, etc.

---

## Part 4: Update Android App

### Step 1: Add HTTP Client Dependencies

Update `app/build.gradle.kts`:

```kotlin
dependencies {
    // ... existing dependencies
    
    // HTTP Client for API calls
    implementation("io.ktor:ktor-client-android:2.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("io.ktor:ktor-client-logging:2.3.0")
}
```

### Step 2: Create API Service

Create `app/src/main/java/com/example/marikitiapp/ui/sharon/data/MpesaApiService.kt`:

```kotlin
package com.example.marikitiapp.ui.sharon.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    val error: String? = null
)

class MpesaApiService {
    // TODO: Replace with your backend URL
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
    }
    
    suspend fun initiateStkPush(request: StkPushRequest): Result<StkPushResponse> {
        return try {
            val response = client.post("$baseUrl/api/mpesa/stk-push") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val result = response.body<StkPushResponse>()
                Result.success(result)
            } else {
                val errorBody = response.body<StkPushResponse>()
                Result.failure(Exception(errorBody.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Step 3: Update PaymentRepository

Update `PaymentRepository.processMpesaPayment()` to call the real API.

---

## Part 5: Testing

### Sandbox Testing

1. Use test credentials
2. Use test phone: `254708374149`
3. Test with small amounts (KES 1-100)
4. Check M-Pesa callback logs

### Production Testing

1. Get production credentials
2. Use real phone numbers
3. Test with real amounts
4. Monitor transactions in M-Pesa portal

---

## Next Steps

1. Set up M-Pesa developer account
2. Get API credentials
3. Choose backend option
4. Deploy backend
5. Update Android app
6. Test integration

Let me know which backend option you prefer, and I'll help you implement it!


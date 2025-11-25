# M-Pesa Integration Testing Guide

## Current Status: ⚠️ Placeholder Implementation

The M-Pesa integration is currently a **placeholder/simulation**. It:
- ✅ Creates payment records in Firestore
- ✅ Shows phone number input field
- ✅ Simulates payment processing (2-second delay)
- ❌ Does NOT actually send STK Push to user's phone
- ❌ Does NOT connect to real M-Pesa API

---

## How to Test Current Implementation

### Step 1: Test the UI Flow

1. **Add items to cart**
   - Go to Product Details
   - Click "Add to Cart"
   - Click "View Cart"

2. **Navigate to Payment**
   - Click "Proceed to Checkout"
   - You should see Payment Method screen

3. **Select M-Pesa**
   - Click on "M-Pesa" option
   - Phone number input field should appear

4. **Enter Phone Number**
   - Format: `254712345678` (254 + 9 digits)
   - Example: `254712345678`
   - The "Pay Now" button should become enabled

5. **Process Payment**
   - Click "Pay Now"
   - You should see "Processing Payment..." message
   - After 2 seconds, it will either:
     - Navigate to Success screen (if simulated success)
     - Navigate to Cancel screen (if simulated failure)

---

## Step 2: Verify in Firebase Console

After processing a payment, check Firebase:

### Check Orders Collection
1. Go to Firebase Console → Firestore Database
2. Open `orders` collection
3. You should see a new order with:
   - `paymentMethod`: `"mpesa"`
   - `paymentStatus`: `"pending"`
   - `deliveryStatus`: `"pending"`

### Check Payments Collection
1. Open `payments` collection
2. You should see a new payment document with:
   - `method`: `"mpesa"`
   - `status`: `"pending"`
   - `phoneNumber`: `"254712345678"` (the number you entered)
   - `amount`: The total amount
   - `orderId`: Reference to the order

---

## Step 3: Test Escrow System

1. **Find your order** in Firestore `orders` collection
2. **Update delivery status**:
   - Click on the order document
   - Change `deliveryStatus` from `"pending"` to `"delivered"`
   - Click "Update"
3. **Verify payment status**:
   - Check the same order document
   - `paymentStatus` should automatically change to `"complete"`
   - This confirms the escrow system is working!

---

## What's Working ✅

- ✅ UI flow (phone input, payment selection)
- ✅ Payment record creation in Firestore
- ✅ Order creation with payment method
- ✅ Escrow logic (payment pending until delivery)
- ✅ Error handling
- ✅ Loading states

---

## What's NOT Working ❌ (Needs Real Integration)

- ❌ Actual STK Push to user's phone
- ❌ Real M-Pesa API connection
- ❌ Payment confirmation from M-Pesa
- ❌ Webhook handling for payment status updates
- ❌ Transaction ID from M-Pesa

---

## How to Implement Real M-Pesa Integration

### Option 1: Use M-Pesa Daraja API (Recommended)

You'll need to:

1. **Set up M-Pesa Developer Account**
   - Register at https://developer.safaricom.co.ke/
   - Get Consumer Key and Consumer Secret
   - Get Shortcode and Passkey

2. **Create Backend Service**
   - Create a backend API (Node.js, Python, etc.)
   - Implement STK Push initiation
   - Handle M-Pesa callbacks/webhooks
   - Update Firestore with payment status

3. **Update PaymentRepository**
   - Replace placeholder with HTTP call to your backend
   - Backend will call M-Pesa API
   - Backend will handle webhook responses

### Option 2: Use Third-Party Service

Services like:
- **Flutterwave** (has M-Pesa integration)
- **Paystack** (has M-Pesa integration)
- **Pesapal** (Kenyan payment gateway)

These provide SDKs and handle M-Pesa integration for you.

---

## Code Changes Needed for Real Integration

### Update `PaymentRepository.processMpesaPayment()`

**Current (Placeholder):**
```kotlin
suspend fun processMpesaPayment(...): Result<String> = try {
    kotlinx.coroutines.delay(2000) // Simulate
    val paymentResult = createPayment(...)
    // ...
}
```

**Real Implementation (Example):**
```kotlin
suspend fun processMpesaPayment(
    phoneNumber: String,
    amount: Double,
    orderId: String
): Result<String> = try {
    // 1. Create payment record first
    val paymentResult = createPayment(orderId, "mpesa", amount, phoneNumber)
    if (paymentResult.isFailure) {
        return paymentResult
    }
    val paymentId = paymentResult.getOrNull() ?: return Result.failure(Exception("No payment ID"))
    
    // 2. Call your backend API
    val response = httpClient.post("https://your-backend.com/api/mpesa/stk-push") {
        contentType(ContentType.Application.Json)
        body = json {
            "phoneNumber" to phoneNumber
            "amount" to amount
            "orderId" to orderId
            "paymentId" to paymentId
        }
    }
    
    // 3. Backend will:
    //    - Call M-Pesa STK Push API
    //    - Send STK Push to user's phone
    //    - Handle webhook when user confirms
    //    - Update Firestore payment status
    
    Result.success(paymentId)
} catch (e: Exception) {
    Result.failure(e)
}
```

### Add HTTP Client Dependency

In `app/build.gradle.kts`:
```kotlin
dependencies {
    // ... existing dependencies
    implementation("io.ktor:ktor-client-android:2.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
}
```

---

## Testing Checklist

### Current Placeholder Testing
- [ ] Can select M-Pesa payment method
- [ ] Phone number input appears and works
- [ ] Payment processing shows loading state
- [ ] Payment record created in Firestore
- [ ] Order created with correct payment method
- [ ] Escrow system works (delivery → payment complete)

### Real Integration Testing (When Implemented)
- [ ] STK Push received on phone
- [ ] Can enter M-Pesa PIN
- [ ] Payment confirmation received
- [ ] Payment status updates in Firestore
- [ ] Transaction ID stored
- [ ] Error handling for failed payments
- [ ] Timeout handling

---

## Common Issues

### Issue: Phone number not accepted
**Solution**: Make sure format is `254XXXXXXXXX` (no spaces, no + sign)

### Issue: Payment always fails
**Solution**: Check Firestore security rules allow write access to `payments` collection

### Issue: No STK Push received
**Solution**: This is expected with placeholder. Real integration requires backend API.

---

## Next Steps

1. **For Development/Testing**: Current placeholder is sufficient
2. **For Production**: 
   - Set up M-Pesa Daraja API account
   - Create backend service
   - Update PaymentRepository
   - Test with real M-Pesa sandbox environment
   - Get approval for production credentials

---

## Resources

- [M-Pesa Daraja API Documentation](https://developer.safaricom.co.ke/APIs)
- [STK Push API Guide](https://developer.safaricom.co.ke/APIs/MpesaStkPushV1)
- [M-Pesa Webhooks](https://developer.safaricom.co.ke/APIs/MpesaExpressSimulate)


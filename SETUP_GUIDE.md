# Complete Setup Guide

## ✅ Step-by-Step Instructions

### 1. **Add Firebase Configuration File**

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or select existing)
3. Click "Add app" → Select Android
4. Enter package name: `com.example.marikitiapp`
5. Download `google-services.json`
6. **Place the file in:** `app/google-services.json` (same folder as `app/build.gradle.kts`)

### 2. **Create Firestore Database**

1. In Firebase Console → Go to "Firestore Database"
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select a location (choose closest to your users)
5. Click "Enable"

### 3. **Create Test Products (Choose ONE method)**

#### Option A: Using the Test Helper (Recommended)
1. Open the app in Android Studio
2. In `MainActivity.kt`, temporarily add this code in `onCreate()`:
```kotlin
import com.example.marikitiapp.ui.sharon.data.TestDataHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// In onCreate(), before setContent:
CoroutineScope(Dispatchers.IO).launch {
    TestDataHelper.createTestProducts()
}
```
3. Run the app once
4. Remove the code after products are created

#### Option B: Manual Creation in Firebase Console
1. Go to Firestore Database
2. Click "Start collection"
3. Collection ID: `products`
4. Add a document with these fields:
   - `name` (string): "Test Product"
   - `description` (string): "A test product"
   - `price` (number): 99.99
   - `imageUrl` (string): ""
   - `sellerId` (string): "test_seller"
   - `rating` (number): 4.5
   - `reviewCount` (number): 10
   - `inStock` (boolean): true
   - `timestamp` (timestamp): [current time]

### 4. **Get Product ID for Testing**

1. After creating products, go to Firestore
2. Click on a product document
3. Copy the **Document ID** (e.g., `abc123xyz`)
4. Update `MainActivity.kt` to use this ID:

```kotlin
SharonNavigation(
    navController = navController,
    startDestination = Screen.ProductDetails.createRoute("YOUR_PRODUCT_ID_HERE")
)
```

### 5. **Build and Run**

1. Sync Gradle files (File → Sync Project with Gradle Files)
2. Build the project (Build → Make Project)
3. Run on device/emulator

### 6. **Test the Flow**

1. **Product Details**: Should load product from Firestore
2. **Add to Cart**: Click "Add to Cart" → Should show popup
3. **My Cart**: View cart items
4. **Payment Method**: 
   - Select M-Pesa → Enter phone number (format: 254712345678)
   - OR Select Cash
5. **Payment Processing**: Will create order and payment in Firestore
6. **Success/Cancel**: Based on payment result

### 7. **Verify in Firebase Console**

After making a purchase:
- Check `orders` collection → Should see new order with `paymentStatus: "pending"`
- Check `payments` collection → Should see payment record
- Update `deliveryStatus` to `"delivered"` in order → `paymentStatus` should auto-update to `"complete"`

## 🔧 Troubleshooting

### Error: "google-services.json not found"
- Make sure file is in `app/` folder (not `app/src/main/`)
- File name must be exactly `google-services.json`

### Error: "Permission denied" in Firestore
- Go to Firestore → Rules
- For testing, use:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```
⚠️ **Warning**: This allows anyone to read/write. Use proper security rules for production!

### Products not loading
- Check internet connection
- Verify product exists in Firestore
- Check product ID matches in navigation
- Check Logcat for error messages

### Payment not working
- M-Pesa requires backend API integration (currently placeholder)
- Cash payments should work immediately
- Check Firestore for created orders/payments

## 📱 Next Steps

1. **Integrate Firebase Authentication** (update `AuthRepository`)
2. **Add M-Pesa Backend API** (update `PaymentRepository.processMpesaPayment()`)
3. **Add Firestore Security Rules** (for production)
4. **Add product images** (update `imageUrl` fields)
5. **Implement delivery confirmation UI** (for sellers)

## 📚 Files to Review

- `FIREBASE_SETUP.md` - Firebase schema documentation
- `app/src/main/java/com/example/marikitiapp/ui/sharon/data/TestDataHelper.kt` - Test data helper
- `app/src/main/java/com/example/marikitiapp/ui/sharon/data/` - Repository classes


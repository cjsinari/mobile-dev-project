# Firestore Security Rules Setup

## Quick Fix for Development

Your app is getting `PERMISSION_DENIED` errors because Firestore security rules are blocking access.

### Step 1: Go to Firebase Console
1. Open [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Click **"Firestore Database"** in the left sidebar
4. Click on the **"Rules"** tab at the top

### Step 2: Update Security Rules (For Development)

Replace the existing rules with these **temporary development rules**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write access to all documents for development
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### Step 3: Publish Rules
1. Click **"Publish"** button
2. Wait a few seconds for rules to update

### Step 4: Test Again
- Restart your app
- The permission errors should be gone
- You should be able to read products and create orders

---

## ⚠️ IMPORTANT: Production Rules

**DO NOT use the above rules in production!** They allow anyone to read/write your database.

For production, use proper security rules like:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Products: Anyone can read, only authenticated sellers can write
    match /products/{productId} {
      allow read: if true;
      allow write: if request.auth != null && 
                      request.resource.data.sellerId == request.auth.uid;
    }
    
    // Orders: Users can only read/write their own orders
    match /orders/{orderId} {
      allow read, write: if request.auth != null && 
                            (resource.data.buyerId == request.auth.uid ||
                             resource.data.sellerId == request.auth.uid);
    }
    
    // Payments: Users can only read their own payments
    match /payments/{paymentId} {
      allow read: if request.auth != null;
      allow write: if false; // Only backend can write payments
    }
  }
}
```

---

## Current Error Details

From your logs:
- ❌ **Read failed**: `Query(products/test_product_1)` - Permission denied
- ❌ **Write failed**: `products/xDlUDxXMmlZfOPkXYIck` - Permission denied
- ❌ **Test data creation failed**: Missing permissions

After updating the rules, all these should work!


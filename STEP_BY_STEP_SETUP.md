# Complete Step-by-Step Setup Guide

## 📋 Step 1: Create Firebase Project

### What you need:
- A Google account
- Access to [Firebase Console](https://console.firebase.google.com/)

### Detailed Instructions:

1. **Go to Firebase Console**
   - Open your browser
   - Navigate to: https://console.firebase.google.com/
   - Sign in with your Google account

2. **Create a New Project**
   - Click the **"Add project"** button (or "Create a project")
   - Enter project name: `MarikitiApp` (or any name you prefer)
   - Click **"Continue"**

3. **Configure Google Analytics (Optional)**
   - You can enable or disable Google Analytics
   - For this project, you can disable it (toggle it off)
   - Click **"Create project"**

4. **Wait for Project Creation**
   - Firebase will create your project (takes 10-30 seconds)
   - Click **"Continue"** when done

---

## 📱 Step 2: Add Android App to Firebase

### What you're doing:
- Registering your Android app with Firebase
- Getting the `google-services.json` configuration file

### Detailed Instructions:

1. **In Firebase Console Dashboard**
   - You should see your project dashboard
   - Look for a section that says "Get started by adding Firebase to your app"
   - Click the **Android icon** (or click "Add app" → Select Android)

2. **Register Your App**
   - **Android package name**: Enter exactly: `com.example.marikitiapp`
     - ⚠️ **IMPORTANT**: This must match your `applicationId` in `app/build.gradle.kts`
   - **App nickname** (optional): `Marikiti App`
   - **Debug signing certificate SHA-1** (optional): Leave blank for now
   - Click **"Register app"**

3. **Download google-services.json**
   - Firebase will show you a download button
   - Click **"Download google-services.json"**
   - The file will download to your computer (usually in Downloads folder)

4. **Move the File to Your Project**
   - Open your project in Android Studio
   - Navigate to the `app` folder in the project structure
   - **Copy** the downloaded `google-services.json` file
   - **Paste** it directly into the `app` folder
   - The file structure should look like this:
     ```
     app/
       ├── google-services.json  ← HERE
       ├── build.gradle.kts
       ├── proguard-rules.pro
       └── src/
   ```

5. **Verify File Location**
   - The file should be at: `app/google-services.json`
   - NOT in `app/src/main/` or anywhere else
   - Right-click the file in Android Studio → "Open in Explorer" to verify location

---

## 🔥 Step 3: Create Firestore Database

### What you're doing:
- Setting up the database where products, orders, and payments will be stored

### Detailed Instructions:

1. **Navigate to Firestore**
   - In Firebase Console, click on **"Firestore Database"** in the left sidebar
   - If you don't see it, click the "Build" menu and select "Firestore Database"

2. **Create Database**
   - Click the **"Create database"** button
   - Choose **"Start in test mode"** (for development)
     - ⚠️ This allows read/write access. For production, you'll need security rules.
   - Click **"Next"**

3. **Select Location**
   - Choose a location closest to your users
   - For Kenya/East Africa, select: `europe-west1` or `us-central1`
   - Click **"Enable"**

4. **Wait for Database Creation**
   - Firebase will create your database (takes 30-60 seconds)
   - You'll see an empty database with no collections yet

---

## 📦 Step 4: Create Test Products

### What you're doing:
- Adding sample products to Firestore so your app has data to display

### Method A: Using Test Helper (Easiest)

1. **Open MainActivity.kt**
   - In Android Studio, navigate to: `app/src/main/java/com/example/marikitiapp/MainActivity.kt`

2. **Temporarily Add Test Code**
   - Add these imports at the top:
   ```kotlin
   import com.example.marikitiapp.ui.sharon.data.TestDataHelper
   import kotlinx.coroutines.CoroutineScope
   import kotlinx.coroutines.Dispatchers
   import kotlinx.coroutines.launch
   ```

3. **Add Code in onCreate()**
   - Modify the `onCreate()` method to look like this:
   ```kotlin
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       enableEdgeToEdge()
       
       // TEMPORARY: Create test products (remove after first run)
       CoroutineScope(Dispatchers.IO).launch {
           try {
               TestDataHelper.createTestProducts()
               println("✅ Test products created successfully!")
           } catch (e: Exception) {
               println("❌ Error creating products: ${e.message}")
           }
       }
       
       setContent {
           // ... rest of the code stays the same
       }
   }
   ```

4. **Run the App**
   - Click the green "Run" button in Android Studio
   - Wait for the app to launch
   - Check Logcat (bottom panel) for: "✅ Test products created successfully!"

5. **Verify in Firebase Console**
   - Go back to Firebase Console → Firestore Database
   - You should see a `products` collection
   - Click on it to see 3 test products

6. **Remove Test Code**
   - Delete the test code you added (the CoroutineScope block)
   - Keep the rest of the code unchanged

### Method B: Manual Creation (Alternative)

1. **In Firebase Console → Firestore Database**
   - Click **"Start collection"**

2. **Create Collection**
   - Collection ID: `products`
   - Click **"Next"**

3. **Add First Document**
   - Document ID: Click "Auto-ID" (let Firebase generate it)
   - Add these fields one by one:
     - Field: `name`, Type: `string`, Value: `Premium Wireless Headphones`
     - Field: `description`, Type: `string`, Value: `Experience premium sound quality...`
     - Field: `price`, Type: `number`, Value: `199.99`
     - Field: `originalPrice`, Type: `number`, Value: `249.99`
     - Field: `imageUrl`, Type: `string`, Value: `` (empty)
     - Field: `sellerId`, Type: `string`, Value: `seller_001`
     - Field: `rating`, Type: `number`, Value: `4.5`
     - Field: `reviewCount`, Type: `number`, Value: `234`
     - Field: `inStock`, Type: `boolean`, Value: `true`
     - Field: `timestamp`, Type: `timestamp`, Value: Click "Set" → Current time
   - Click **"Save"**

4. **Repeat for More Products** (optional)
   - Click "Add document" to create more test products

---

## 🔑 Step 5: Get Product ID

### What you're doing:
- Finding the ID of a product so your app knows which product to display

### Detailed Instructions:

1. **In Firebase Console → Firestore Database**
   - Click on the `products` collection
   - You'll see a list of products (documents)

2. **Copy Document ID**
   - Click on any product document
   - Look at the top of the document
   - You'll see something like: `Document ID: abc123xyz456`
   - **Copy this ID** (the random string)

3. **Update MainActivity.kt**
   - Open `app/src/main/java/com/example/marikitiapp/MainActivity.kt`
   - Find this line:
   ```kotlin
   SharonNavigation(navController = navController)
   ```
   - Change it to:
   ```kotlin
   SharonNavigation(
       navController = navController,
       startDestination = Screen.ProductDetails.createRoute("YOUR_PRODUCT_ID_HERE")
   )
   ```
   - Replace `YOUR_PRODUCT_ID_HERE` with the ID you copied
   - Example: `Screen.ProductDetails.createRoute("abc123xyz456")`

4. **Alternative: Use Default Test ID**
   - If you used TestDataHelper, you can check Logcat for the created IDs
   - Or use the first product ID you see in Firestore

---

## 🔨 Step 6: Build and Run

### What you're doing:
- Compiling the app and running it on your device/emulator

### Detailed Instructions:

1. **Sync Gradle**
   - In Android Studio, click: **File → Sync Project with Gradle Files**
   - Wait for sync to complete (check bottom status bar)

2. **Check for Errors**
   - Look at the "Build" tab at the bottom
   - If you see errors, they'll be highlighted in red
   - Common issues:
     - Missing `google-services.json` → Make sure it's in `app/` folder
     - Gradle sync failed → Check internet connection

3. **Connect Device or Emulator**
   - **Physical Device**: 
     - Enable USB debugging on your phone
     - Connect via USB
     - Allow USB debugging when prompted
   - **Emulator**:
     - Click "Device Manager" in Android Studio
     - Create/Start an emulator

4. **Run the App**
   - Click the green **"Run"** button (or press Shift+F10)
   - Select your device/emulator
   - Wait for the app to build and install

5. **What to Expect**
   - App should launch
   - You should see the Product Details screen
   - Product information should load from Firestore
   - If you see "Loading..." or "Error loading product", check:
     - Internet connection
     - Product ID is correct
     - Product exists in Firestore

---

## ✅ Step 7: Test the Complete Flow

### Test Each Screen:

1. **Product Details Screen**
   - ✅ Should show product name, price, description
   - ✅ Should have quantity selector (+/-)
   - ✅ Click "Add to Cart" button

2. **Add to Cart Popup**
   - ✅ Should appear as a dialog
   - ✅ Shows product name and quantity
   - ✅ Click "View Cart"

3. **My Cart Screen**
   - ✅ Shows items in cart
   - ✅ Can change quantities
   - ✅ Shows total price
   - ✅ Click "Proceed to Checkout"

4. **Payment Method Screen**
   - ✅ Should show ONLY 2 options: M-Pesa and Cash
   - ✅ If M-Pesa selected, phone number input appears
   - ✅ Enter phone: `254712345678` (format: 254XXXXXXXXX)
   - ✅ Click "Pay Now"

5. **Payment Processing**
   - ✅ Shows "Processing Payment..." message
   - ✅ Creates order in Firestore
   - ✅ Creates payment record

6. **Success/Cancel Screen**
   - ✅ Based on payment result

---

## 🔍 Step 8: Verify in Firebase Console

### Check What Was Created:

1. **Go to Firestore Database**

2. **Check Orders Collection**
   - After making a purchase, you should see an `orders` collection
   - Click on it to see your order
   - Check that:
     - `paymentStatus` = `"pending"`
     - `deliveryStatus` = `"pending"`
     - `paymentMethod` = `"mpesa"` or `"cash"`

3. **Check Payments Collection**
   - You should see a `payments` collection
   - Contains payment records with:
     - `status` = `"pending"`
     - `method` = `"mpesa"` or `"cash"`

4. **Test Escrow System**
   - In Firestore, find your order
   - Click on it
   - Change `deliveryStatus` from `"pending"` to `"delivered"`
   - Click "Update"
   - Check `paymentStatus` → Should automatically change to `"complete"`

---

## 🐛 Troubleshooting

### Problem: "google-services.json not found"
**Solution:**
- Verify file is in `app/` folder (not `app/src/main/`)
- File name must be exactly `google-services.json` (case-sensitive)
- In Android Studio, right-click `app` folder → "Show in Explorer" to verify

### Problem: "Permission denied" in Firestore
**Solution:**
- Go to Firebase Console → Firestore → Rules
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
- Click "Publish"

### Problem: Products not loading
**Solution:**
- Check internet connection on device/emulator
- Verify product ID in MainActivity matches Firestore document ID
- Check Logcat for error messages
- Verify product exists in Firestore `products` collection

### Problem: App crashes on launch
**Solution:**
- Check Logcat for error messages
- Verify `google-services.json` is in correct location
- Make sure Gradle sync completed successfully
- Try: Build → Clean Project, then Build → Rebuild Project

### Problem: Payment not working
**Solution:**
- M-Pesa is placeholder (needs backend API)
- Cash payments should work
- Check Firestore for created orders/payments
- Verify cart has items before checkout

---

## 📝 Next Steps After Setup

1. **Add Real Product Images**
   - Upload images to Firebase Storage
   - Update `imageUrl` in products

2. **Set Up Firebase Authentication**
   - Implement user login/signup
   - Update `AuthRepository` to use real user IDs

3. **Configure M-Pesa API**
   - Set up backend service
   - Update `PaymentRepository.processMpesaPayment()`

4. **Add Security Rules**
   - Create proper Firestore security rules
   - Restrict access based on user authentication

5. **Test Delivery Confirmation**
   - Build UI for sellers to confirm delivery
   - Test escrow fund release

---

## 📞 Need Help?

If you get stuck at any step:
1. Check Logcat for error messages
2. Verify each step was completed correctly
3. Check Firebase Console to see if data was created
4. Review the error message and search for solutions

Good luck! 🚀


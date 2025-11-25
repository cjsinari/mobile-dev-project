# Real M-Pesa Integration - Step by Step

## ✅ What I've Done For You

1. ✅ Added Ktor HTTP client dependencies
2. ✅ Created `MpesaApiService.kt` - API service for backend calls
3. ✅ Updated `PaymentRepository.kt` - Now calls real backend API
4. ✅ Created complete backend server (`mpesa-backend/server.js`)
5. ✅ Created backend setup files

---

## 📋 Your Action Items

### Step 1: Get M-Pesa Developer Account (15 minutes)

1. Go to https://developer.safaricom.co.ke/
2. Click **"Get API Credentials"** or **"Sign Up"**
3. Register your account
4. Verify email
5. Create a new app
6. **Save these credentials:**
   - Consumer Key
   - Consumer Secret
   - Shortcode (for sandbox: `174379`)
   - Passkey (provided in dashboard)

### Step 2: Set Up Backend Server (30 minutes)

#### Option A: Local Testing First

1. **Open terminal in `mpesa-backend` folder:**
   ```bash
   cd mpesa-backend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Create `.env` file:**
   ```bash
   # Copy the example
   cp .env.example .env
   ```

4. **Edit `.env` file with your credentials:**
   ```env
   MPESA_CONSUMER_KEY=your_consumer_key_here
   MPESA_CONSUMER_SECRET=your_consumer_secret_here
   MPESA_SHORTCODE=174379
   MPESA_PASSKEY=your_passkey_here
   MPESA_CALLBACK_URL=http://localhost:3000/api/mpesa/callback
   MPESA_ENVIRONMENT=sandbox
   PORT=3000
   ```

5. **For local testing, use ngrok:**
   - Install ngrok: https://ngrok.com/
   - Start your server: `npm start`
   - In another terminal: `ngrok http 3000`
   - Copy the HTTPS URL (e.g., `https://abc123.ngrok.io`)
   - Update `.env`: `MPESA_CALLBACK_URL=https://abc123.ngrok.io/api/mpesa/callback`
   - Restart server

6. **Start server:**
   ```bash
   npm start
   ```

#### Option B: Deploy to Cloud (Recommended)

**Heroku (Free):**
```bash
cd mpesa-backend
heroku create your-app-name
heroku config:set MPESA_CONSUMER_KEY=your_key
heroku config:set MPESA_CONSUMER_SECRET=your_secret
heroku config:set MPESA_SHORTCODE=174379
heroku config:set MPESA_PASSKEY=your_passkey
heroku config:set MPESA_CALLBACK_URL=https://your-app-name.herokuapp.com/api/mpesa/callback
heroku config:set MPESA_ENVIRONMENT=sandbox
git init
git add .
git commit -m "Initial commit"
git push heroku main
```

**Railway (Easier):**
1. Go to https://railway.app
2. New Project → Deploy from GitHub
3. Connect your repo
4. Add environment variables in dashboard
5. Deploy automatically

### Step 3: Update Android App (5 minutes)

1. **Open `MpesaApiService.kt`**
   - Location: `app/src/main/java/com/example/marikitiapp/ui/sharon/data/MpesaApiService.kt`

2. **Update the backend URL:**
   ```kotlin
   // For local testing (emulator):
   private val baseUrl = "http://10.0.2.2:3000"
   
   // OR for deployed backend:
   private val baseUrl = "https://your-backend-url.herokuapp.com"
   ```

3. **Sync Gradle:**
   - File → Sync Project with Gradle Files

### Step 4: Test Integration (10 minutes)

1. **Start backend server** (if local)
2. **Run Android app**
3. **Test flow:**
   - Add product to cart
   - Go to checkout
   - Select M-Pesa
   - Enter test phone: `254708374149` (M-Pesa sandbox test number)
   - Click "Pay Now"
4. **Check backend logs** - Should see API calls
5. **Check phone** - Should receive STK Push (if using real number in production)

---

## 🔍 Verification Checklist

- [ ] Backend server running and accessible
- [ ] M-Pesa credentials configured
- [ ] Android app updated with backend URL
- [ ] Can initiate STK Push from app
- [ ] Backend receives request
- [ ] M-Pesa API called successfully
- [ ] STK Push sent to phone (or test number)
- [ ] Payment record created in Firestore
- [ ] Callback received (check backend logs)

---

## 🐛 Troubleshooting

### Backend not accessible from app
- **Local testing**: Use `http://10.0.2.2:3000` (Android emulator)
- **Deployed**: Check backend URL is correct
- **Network**: Ensure device/emulator has internet

### M-Pesa API errors
- Check credentials are correct
- Verify environment (sandbox vs production)
- Check phone number format (must be 254XXXXXXXXX)
- Verify callback URL is publicly accessible

### STK Push not received
- **Sandbox**: Use test number `254708374149`
- **Production**: Use real M-Pesa registered number
- Check M-Pesa account has sufficient balance
- Verify shortcode is correct

### Payment status not updating
- Backend webhook needs to update Firestore
- Add Firebase Admin SDK to backend
- Implement callback handler to update payment status

---

## 📝 Next: Add Firebase Admin to Backend

To automatically update payment status when M-Pesa confirms payment:

1. **Install Firebase Admin in backend:**
   ```bash
   npm install firebase-admin
   ```

2. **Initialize in `server.js`:**
   ```javascript
   const admin = require('firebase-admin');
   const serviceAccount = require('./path/to/serviceAccountKey.json');
   
   admin.initializeApp({
     credential: admin.credential.cert(serviceAccount)
   });
   
   const db = admin.firestore();
   ```

3. **Update callback handler** to update Firestore when payment succeeds

See `REAL_MPESA_INTEGRATION_GUIDE.md` for complete code examples.

---

## 🎯 Quick Start Summary

1. Get M-Pesa credentials → https://developer.safaricom.co.ke/
2. Set up backend → `cd mpesa-backend && npm install`
3. Configure `.env` → Add your credentials
4. Deploy backend → Heroku/Railway
5. Update Android app → Change `baseUrl` in `MpesaApiService.kt`
6. Test → Run app and try payment

**You're ready to go!** 🚀


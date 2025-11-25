# M-Pesa Backend Setup Guide

## Quick Start: Node.js Backend

### Step 1: Create Backend Project

```bash
# Create new directory
mkdir mpesa-backend
cd mpesa-backend

# Initialize Node.js project
npm init -y

# Install dependencies
npm install express axios dotenv cors
```

### Step 2: Create Files

**Create `server.js`** (copy from REAL_MPESA_INTEGRATION_GUIDE.md)

**Create `.env` file:**
```env
MPESA_CONSUMER_KEY=your_consumer_key_here
MPESA_CONSUMER_SECRET=your_consumer_secret_here
MPESA_SHORTCODE=174379
MPESA_PASSKEY=your_passkey_here
MPESA_CALLBACK_URL=https://your-backend-url.com/api/mpesa/callback
MPESA_ENVIRONMENT=sandbox
PORT=3000
```

### Step 3: Run Backend

```bash
node server.js
```

### Step 4: Deploy Backend

#### Option A: Heroku (Free)

1. Install Heroku CLI
2. Login: `heroku login`
3. Create app: `heroku create your-app-name`
4. Set environment variables in Heroku dashboard
5. Deploy: `git push heroku main`

#### Option B: Railway (Easy)

1. Go to https://railway.app
2. New Project → Deploy from GitHub
3. Add environment variables
4. Deploy automatically

#### Option C: Firebase Cloud Functions

See Firebase documentation for Cloud Functions setup.

---

## Update Android App Backend URL

After deploying, update `MpesaApiService.kt`:

```kotlin
private val baseUrl = "https://your-deployed-backend-url.com"
```

For local testing on emulator:
```kotlin
private val baseUrl = "http://10.0.2.2:3000"
```

---

## Testing

1. Start backend server
2. Update Android app with backend URL
3. Test STK Push from app
4. Check backend logs for API calls
5. Verify M-Pesa callback received


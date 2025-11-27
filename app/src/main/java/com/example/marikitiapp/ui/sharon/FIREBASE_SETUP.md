# Firebase Setup Instructions

## Required Setup Steps

1. **Add google-services.json**
   - Download your `google-services.json` file from Firebase Console
   - Place it in `app/` directory (same level as `build.gradle.kts`)

2. **Firebase Collections Schema**

   The app expects the following Firestore collections:

   ### Products Collection
   ```
   products/
     {productId}/
       - name: String
       - description: String
       - price: Double
       - originalPrice: Double? (optional)
       - imageUrl: String
       - sellerId: String
       - rating: Double
       - reviewCount: Long
       - inStock: Boolean
       - timestamp: Timestamp
   ```

   ### Orders Collection
   ```
   orders/
     {orderId}/
       - buyerId: String
       - paymentMethod: String ("mpesa" or "cash")
       - paymentStatus: String ("pending", "complete", "cancelled")
       - deliveryStatus: String ("pending", "delivered")
       - items: Array<Map>
         - productId: String
         - productName: String
         - price: Double
         - quantity: Long
       - totalAmount: Double
       - timestamp: Timestamp
   ```

   ### Payments Collection
   ```
   payments/
     {paymentId}/
       - orderId: String
       - method: String ("mpesa" or "cash")
       - amount: Double
       - status: String ("pending", "complete", "refunded")
       - phoneNumber: String? (for M-Pesa)
       - timestamp: Timestamp
   ```

3. **M-Pesa Integration**
   - The M-Pesa STK Push integration is structured but requires backend API
   - Update `PaymentRepository.processMpesaPayment()` with your M-Pesa API credentials
   - Configure M-Pesa API credentials in your backend service

4. **Firebase Security Rules**
   - Set up appropriate Firestore security rules for your collections
   - Ensure users can read products and create orders
   - Restrict write access to orders and payments appropriately

5. **Authentication**
   - Currently uses placeholder user ID
   - Integrate Firebase Authentication in `AuthRepository` for production use

## Escrow Payment Flow

1. **Payment Creation**: Payment is created with status "pending"
2. **Order Creation**: Order is created with paymentStatus "pending" and deliveryStatus "pending"
3. **Delivery Confirmation**: When deliveryStatus is updated to "delivered", paymentStatus automatically changes to "complete"
4. **Funds Release**: Seller receives funds only after delivery confirmation

## Testing

- Use Firebase Console to manually create test products
- Test payment flow with both M-Pesa and Cash options
- Verify escrow logic by updating delivery status in Firestore


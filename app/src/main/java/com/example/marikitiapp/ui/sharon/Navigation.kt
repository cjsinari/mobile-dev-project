package com.example.marikitiapp.ui.sharon

sealed class Screen(val route: String) {
    object ProductDetails : Screen("product_details/{productId}") {
        fun createRoute(productId: String) = "product_details/$productId"
    }
    object MyCart : Screen("my_cart")
    object PaymentMethod : Screen("payment_method")
    object Success : Screen("success")
    object CancelPaymentConfirmation : Screen("cancel_payment_confirmation")
}


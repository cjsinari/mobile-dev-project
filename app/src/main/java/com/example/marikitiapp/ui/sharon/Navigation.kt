package com.example.marikitiapp.ui.sharon

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int? = null,
    val title: String = "",
    val icon: ImageVector? = null,
    val showInNavigation: Boolean = false,
    val requiresAuth: Boolean = false
) {
    // Product Screens
    object ProductList : Screen(
        route = "product_list",
        title = "Products",
        icon = Icons.Default.ShoppingBag,
        showInNavigation = true
    )

    object ProductDetails : Screen(
        route = "product_details/{productId}",
        title = "Product Details"
    ) {
        fun createRoute(productId: String) = "product_details/$productId"
        const val ARG_PRODUCT_ID = "productId"
    }

    object ProductSearch : Screen(
        route = "product_search",
        title = "Search Products",
        icon = Icons.Default.Search,
        showInNavigation = false
    )

    // Cart & Checkout Screens
    object MyCart : Screen(
        route = "my_cart",
        title = "My Cart",
        icon = Icons.Default.ShoppingCart,
        showInNavigation = true
    )

    object Checkout : Screen(
        route = "checkout",
        title = "Checkout",
        requiresAuth = true
    )

    object PaymentMethod : Screen(
        route = "payment_method",
        title = "Payment Method",
        requiresAuth = true
    )

    object AddCard : Screen(
        route = "add_card",
        title = "Add Payment Card",
        requiresAuth = true
    )

    // Order & Success Screens
    object OrderConfirmation : Screen(
        route = "order_confirmation/{orderId}",
        title = "Order Confirmed"
    ) {
        fun createRoute(orderId: String) = "order_confirmation/$orderId"
        const val ARG_ORDER_ID = "orderId"
    }

    object Success : Screen(
        route = "success/{orderId}",
        title = "Payment Successful"
    ) {
        fun createRoute(orderId: String) = "success/$orderId"
        const val ARG_ORDER_ID = "orderId"
    }

    object OrderHistory : Screen(
        route = "order_history",
        title = "My Orders",
        icon = Icons.Default.History,
        showInNavigation = true,
        requiresAuth = true
    )

    object OrderDetails : Screen(
        route = "order_details/{orderId}",
        title = "Order Details",
        requiresAuth = true
    ) {
        fun createRoute(orderId: String) = "order_details/$orderId"
        const val ARG_ORDER_ID = "orderId"
    }

    // User & Account Screens
    object Profile : Screen(
        route = "profile",
        title = "My Profile",
        icon = Icons.Default.Person,
        showInNavigation = true,
        requiresAuth = true
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings,
        showInNavigation = true
    )

    object AddressBook : Screen(
        route = "address_book",
        title = "My Addresses",
        requiresAuth = true
    )

    // Authentication Screens
    object Login : Screen(
        route = "login",
        title = "Sign In",
        requiresAuth = false
    )

    object Register : Screen(
        route = "register",
        title = "Create Account",
        requiresAuth = false
    )

    // Cancel & Error Screens
    object CancelPaymentConfirmation : Screen(
        route = "cancel_payment_confirmation",
        title = "Payment Cancelled"
    )

    object PaymentError : Screen(
        route = "payment_error/{errorCode}",
        title = "Payment Failed"
    ) {
        fun createRoute(errorCode: String) = "payment_error/$errorCode"
        const val ARG_ERROR_CODE = "errorCode"
    }

    object NetworkError : Screen(
        route = "network_error",
        title = "Connection Error"
    )

    // Favorites & Wishlist
    object Favorites : Screen(
        route = "favorites",
        title = "My Favorites",
        icon = Icons.Default.Favorite,
        showInNavigation = true,
        requiresAuth = true
    )

    // Utility functions for navigation
    companion object {
        // Get all screens that should appear in bottom navigation
        val bottomNavigationScreens = listOf(
            ProductList,
            MyCart,
            Favorites,
            OrderHistory,
            Profile
        )

        // Get all screens that should appear in main drawer
        val mainNavigationScreens = listOf(
            ProductList,
            MyCart,
            Favorites,
            OrderHistory,
            Profile,
            Settings
        )

        // Check if a route requires authentication
        fun requiresAuth(route: String?): Boolean {
            return when {
                route == null -> false
                route.contains("success") -> false
                route.contains("cancel") -> false
                route.contains("error") -> false
                else -> mainNavigationScreens.any { screen ->
                    screen.route == route || route.contains(screen.route) && screen.requiresAuth
                }
            }
        }

        // Extract arguments from route
        fun extractProductId(route: String): String? {
            return route.substringAfter("product_details/").takeIf { it != route }
        }

        fun extractOrderId(route: String): String? {
            return when {
                route.contains("order_details/") -> route.substringAfter("order_details/")
                route.contains("success/") -> route.substringAfter("success/")
                route.contains("order_confirmation/") -> route.substringAfter("order_confirmation/")
                else -> null
            }
        }

        // Get screen by route
        fun fromRoute(route: String?): Screen {
            return when {
                route == null -> ProductList
                route.startsWith("product_details/") -> ProductDetails
                route.startsWith("order_details/") -> OrderDetails
                route.startsWith("success/") -> Success
                route.startsWith("order_confirmation/") -> OrderConfirmation
                route.startsWith("payment_error/") -> PaymentError
                else -> mainNavigationScreens.find { it.route == route } ?: ProductList
            }
        }
    }
}

// Extension functions for better navigation experience
fun Screen.getTitle(): String = this.title

fun Screen.getIcon(): ImageVector? = this.icon

fun Screen.shouldShowInNavigation(): Boolean = this.showInNavigation

fun Screen.getRouteWithArgs(vararg args: Pair<String, Any>): String {
    var finalRoute = this.route
    args.forEach { (key, value) ->
        finalRoute = finalRoute.replace("{$key}", value.toString())
    }
    return finalRoute
}

// Data class for navigation arguments
data class NavigationArgs(
    val productId: String? = null,
    val orderId: String? = null,
    val errorCode: String? = null,
    val returnRoute: String? = null
) {
    companion object {
        fun fromRoute(route: String?): NavigationArgs {
            return NavigationArgs(
                productId = Screen.extractProductId(route ?: ""),
                orderId = Screen.extractOrderId(route ?: "")
            )
        }
    }
}

// Navigation state for managing app navigation
data class NavigationState(
    val currentScreen: Screen = Screen.ProductList,
    val previousScreen: Screen? = null,
    val navigationArgs: NavigationArgs = NavigationArgs(),
    val isBottomBarVisible: Boolean = true,
    val isDrawerEnabled: Boolean = true
) {
    fun updateScreen(screen: Screen, args: NavigationArgs = NavigationArgs()): NavigationState {
        return copy(
            currentScreen = screen,
            previousScreen = currentScreen,
            navigationArgs = args,
            isBottomBarVisible = screen.showInNavigation,
            isDrawerEnabled = screen !is Screen.CancelPaymentConfirmation && screen !is Screen.PaymentError
        )
    }
}
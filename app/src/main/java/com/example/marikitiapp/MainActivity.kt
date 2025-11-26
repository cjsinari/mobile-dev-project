package com.example.marikitiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marikitiapp.ui.product.PostProductScreen
import com.example.marikitiapp.ui.product.SuccessScreen
import com.example.marikitiapp.ui.product.YourProductsScreen
import com.example.marikitiapp.ui.dashboard.SellerDashboardScreen
import com.example.marikitiapp.ui.theme.MarikitiAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            MarikitiAppTheme {
                  MaterialTheme(colorScheme = lightColorScheme()) {
                      MarikitiApp()
                  }

                }
            }
        }
    }

@Composable
fun MarikitiApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "seller_dashboard"
    ) {
        composable("seller_dashboard") {
            SellerDashboardScreen(
                onPostProduct = {
                    navController.navigate("post_product") {
                        // Clear back stack to prevent multiple instances
                        launchSingleTop = true
                    }
                },
                onYourProducts = {
                    navController.navigate("your_products") {
                        launchSingleTop = true
                    }
                },
                onAnalytics = {
                    navController.navigate("analytics") {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("post_product") {
            PostProductScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable("success_screen") {
            SuccessScreen(navController)
        }

        composable("your_products") {
            YourProductsScreen(
                navController = navController,
                onBack = {
                    navController.navigate("seller_dashboard") {
                        // Clear everything and go back to dashboard
                        popUpTo("seller_dashboard") { inclusive = false }
                    }
                },
                onProductClick = { productId ->
                    // Navigate to product detail screen (implement later)
                    // navController.navigate("product_detail/$productId")
                }
            )
        }

        // Optional: Add product detail screen for future
        composable("product_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            // TODO: Implement ProductDetailScreen
            // ProductDetailScreen(
            //     productId = productId,
            //     onBack = { navController.popBackStack() }
            // )
        }

        // Placeholder for analytics screen
        composable("analytics") {
            // TODO: Implement AnalyticsScreen
        }
    }
}
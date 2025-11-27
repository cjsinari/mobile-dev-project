package com.example.marikitiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marikitiapp.ui.sharon.SharonNavigation
import com.example.marikitiapp.ui.product.PostProductScreen
import com.example.marikitiapp.ui.product.SuccessScreen
import com.example.marikitiapp.ui.product.YourProductsScreen
import com.example.marikitiapp.ui.dashboard.SellerDashboardScreen
import com.example.marikitiapp.DiscoverScreen
import com.example.marikitiapp.LoginScreen
import com.example.marikitiapp.ProductScreen
import com.example.marikitiapp.ProductCard
import com.example.marikitiapp.SignupScreen
import com.example.marikitiapp.ui.theme.MarikitiAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {

    // Firebase Initialization
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            MarikitiAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Combined navigation: Your flows + SharonNavigation
                    val navController: NavHostController = rememberNavController()
                    CombinedNavigation(navController, auth, firestore)
                }
            }
        }
    }
}

@Composable
fun CombinedNavigation(
    navController: NavHostController,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore
) {
    NavHost(
        navController = navController,
        startDestination = "seller_dashboard"
    ) {
        // Jamie's authentication flows
        composable("discover") {
            DiscoverScreen(
                firestore = firestore,
                onSignupClick = { navController.navigate("signup") },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable("signup") {
            SignupScreen(
                auth = auth,
                onLoginClick = { navController.navigate("login") },
                onSignupSuccess = {
                    navController.navigate("seller_dashboard") {
                        popUpTo("discover") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                auth = auth,
                onSignupClick = { navController.navigate("signup") },
                onLoginSuccess = {
                    navController.navigate("seller_dashboard") {
                        popUpTo("discover") { inclusive = true }
                    }
                }
            )
        }

        // Seller dashboard flows
        composable("seller_dashboard") {
            SellerDashboardScreen(
                onPostProduct = { navController.navigate("post_product") },
                onYourProducts = { navController.navigate("your_products") },
                onAnalytics = { navController.navigate("analytics") },
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
                        popUpTo("seller_dashboard") { inclusive = false }
                    }
                },
                onProductClick = { productId ->
                    // navController.navigate("product_detail/$productId")
                }
            )
        }

        // Jamie’s product screen
        composable("product") {
            ProductScreen(auth = auth, firestore = firestore)
        }

        // Placeholder for analytics screen
        composable("analytics") {
            // TODO: Implement AnalyticsScreen
        }
    }
}
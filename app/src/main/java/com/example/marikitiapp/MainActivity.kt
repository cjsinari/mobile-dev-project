package com.example.marikitiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marikitiapp.ui.theme.MarikitiAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {

    // Use lazy initialization for Firebase services for better performance and safety.
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val firestore: FirebaseFirestore by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MarikitiAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController: NavHostController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "discover"
                    ) {
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
                                    // Navigate to discover and clear the back stack
                                    navController.navigate("discover") {
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
                                    // Navigate to discover and clear the back stack
                                    navController.navigate("discover") {
                                        popUpTo("discover") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("product") {
                            // FIX: Pass both auth and firestore to the ProductScreen
                            ProductScreen(auth = auth, firestore = firestore)
                        }
                    }
                }
            }
        }
    }
}






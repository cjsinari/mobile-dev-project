package com.example.marikitiapp.ui.sharon

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun SharonNavigation(
    navController: NavHostController,
    startDestination: String = Screen.ProductDetails.createRoute("XxRewqWC3G7XZ087kg7l") // Use first created product ID
) {
    // Shared ViewModels
    val cartViewModel: CartViewModel = viewModel()
    val productDetailsViewModel: ProductDetailsViewModel = viewModel()
    val paymentViewModel: PaymentViewModel = viewModel { PaymentViewModel(cartViewModel = cartViewModel) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.ProductDetails.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(
                productId = productId,
                cartViewModel = cartViewModel,
                viewModel = productDetailsViewModel,
                onNavigateToCart = {
                    navController.navigate(Screen.MyCart.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.MyCart.route) {
            MyCartScreen(
                viewModel = cartViewModel,
                onNavigateToPayment = {
                    navController.navigate(Screen.PaymentMethod.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PaymentMethod.route) {
            PaymentMethodScreen(
                cartViewModel = cartViewModel,
                paymentViewModel = paymentViewModel,
                onNavigateToSuccess = { orderId ->
                    navController.navigate(Screen.Success.route) {
                        popUpTo(Screen.PaymentMethod.route) { inclusive = true }
                    }
                },
                onNavigateToCancel = {
                    navController.navigate(Screen.CancelPaymentConfirmation.route) {
                        popUpTo(Screen.PaymentMethod.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Success.route) {
            SuccessScreen(
                onContinueShopping = {
                    navController.popBackStack(Screen.ProductDetails.route, inclusive = false)
                }
            )
        }

        composable(Screen.CancelPaymentConfirmation.route) {
            CancelPaymentConfirmationScreen(
                onRetryPayment = {
                    navController.navigate(Screen.PaymentMethod.route) {
                        popUpTo(Screen.CancelPaymentConfirmation.route) { inclusive = true }
                    }
                },
                onBackToCart = {
                    navController.navigate(Screen.MyCart.route) {
                        popUpTo(Screen.CancelPaymentConfirmation.route) { inclusive = true }
                    }
                }
            )
        }
    }
}


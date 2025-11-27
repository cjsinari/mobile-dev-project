package com.example.marikitiapp.ui.sharon

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SharonNavigation(
    navController: NavHostController,
    startDestination: String = Screen.ProductDetails.createRoute("XxRewqWC3G7XZ087kg7l")
) {
    // Shared ViewModels
    val cartViewModel: CartViewModel = viewModel()
    val productDetailsViewModel: ProductDetailsViewModel = viewModel()
    val paymentViewModel: PaymentViewModel = viewModel { PaymentViewModel(cartViewModel = cartViewModel) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Global navigation animations for premium feel
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        // Product Details Screen with custom animations
        composable(
            route = Screen.ProductDetails.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            enterTransition = {
                // Elegant scale + fade for product details
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                // Smooth exit for product details
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                // Elegant return animation
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            popExitTransition = {
                // Scale down when going back from product details
                scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(
                productId = productId,
                cartViewModel = cartViewModel,
                viewModel = productDetailsViewModel,
                onNavigateToCart = {
                    navController.navigate(Screen.MyCart.route) {
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Shopping Cart Screen with slide-up animation
        composable(
            route = Screen.MyCart.route,
            enterTransition = {
                // Slide up from bottom for cart (like a modal)
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                // Slide down when leaving cart
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                // Slide up when returning to cart
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            popExitTransition = {
                // Slide down when going back from cart
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) {
            MyCartScreen(
                viewModel = cartViewModel,
                onNavigateToPayment = {
                    navController.navigate(Screen.PaymentMethod.route) {
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Payment Method Screen with sophisticated entrance
        composable(
            route = Screen.PaymentMethod.route,
            enterTransition = {
                // Elegant slide from right for payment flow
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(600))
            },
            exitTransition = {
                // Quick slide out for payment
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                // Smooth return to payment screen
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            popExitTransition = {
                // Elegant exit when going back from payment
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) {
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

        // Success Screen with celebratory entrance
        composable(
            route = Screen.Success.route,
            enterTransition = {
                // Celebratory scale and fade for success
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(700, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Gentle fade out for success screen
                fadeOut(animationSpec = tween(500))
            },
            popEnterTransition = {
                // No pop enter since we clear back stack
                fadeIn(animationSpec = tween(400))
            },
            popExitTransition = {
                // No pop exit since we clear back stack
                fadeOut(animationSpec = tween(400))
            }
        ) {
            SuccessScreen(
                onContinueShopping = {
                    navController.popBackStack(Screen.ProductDetails.route, inclusive = false)
                }
            )
        }

        // Cancel Payment Confirmation Screen with modal animation
        composable(
            route = Screen.CancelPaymentConfirmation.route,
            enterTransition = {
                // Modal-like scale animation for confirmation
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                // Scale down when leaving confirmation
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                // Scale in when returning to confirmation
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            popExitTransition = {
                // Scale out when going back from confirmation
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) {
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
package com.example.marikitiapp.ui.sharon

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddToCartPopup(
    product: Product?,
    quantity: Int,
    cartViewModel: CartViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Animation states for elegant entrance
    var shouldAnimate by remember { mutableStateOf(false) }
    val dialogScale by animateFloatAsState(
        targetValue = if (shouldAnimate) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialog_scale"
    )

    val dialogAlpha by animateFloatAsState(
        targetValue = if (shouldAnimate) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "dialog_alpha"
    )

    LaunchedEffect(Unit) {
        shouldAnimate = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Brighter surface with white background
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    alpha = dialogAlpha
                },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface, // Already bright from your theme
            shadowElevation = 24.dp, // Increased elevation for more brightness contrast
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Animated Success Icon with brighter colors
                AnimatedSuccessIcon()

                // Title with brighter text
                AnimatedVisibility(
                    visible = shouldAnimate,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { -20 },
                        animationSpec = tween(durationMillis = 400, delayMillis = 100)
                    ),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Added to Cart!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface // Already bright
                    )
                }

                // Product Info with brighter text
                AnimatedProductInfo(
                    product = product,
                    quantity = quantity,
                    shouldAnimate = shouldAnimate
                )

                // Buttons with orange Continue Shopping button
                AnimatedButtonRow(
                    onDismiss = onDismiss,
                    onConfirm = {
                        product?.let {
                            cartViewModel.addItem(it, quantity)
                        }
                        onConfirm()
                    },
                    shouldAnimate = shouldAnimate
                )
            }
        }
    }
}

@Composable
private fun AnimatedSuccessIcon() {
    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "icon_rotation"
    )

    // Brighter container with orange accent
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(48.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer) // Orange container
            .graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
                rotationZ = rotation
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = "Success",
            modifier = Modifier.size(42.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer // Orange text
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedProductInfo(
    product: Product?,
    quantity: Int,
    shouldAnimate: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        product?.let {
            AnimatedVisibility(
                visible = shouldAnimate,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(durationMillis = 400, delayMillis = 200)
                ),
                exit = fadeOut()
            ) {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface // Bright text
                )
            }

            AnimatedVisibility(
                visible = shouldAnimate,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(durationMillis = 400, delayMillis = 300)
                ),
                exit = fadeOut()
            ) {
                Text(
                    text = "Quantity: $quantity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(
                visible = shouldAnimate,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(durationMillis = 400, delayMillis = 400)
                ),
                exit = fadeOut()
            ) {
                Text(
                    text = "Total: ksh${String.format("%.2f", it.price * quantity)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary, // Bright primary color
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedButtonRow(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    shouldAnimate: Boolean
) {
    val continueShoppingInteractionSource = remember { MutableInteractionSource() }
    val continueShoppingPressed by continueShoppingInteractionSource.collectIsPressedAsState()

    val viewCartInteractionSource = remember { MutableInteractionSource() }
    val viewCartPressed by viewCartInteractionSource.collectIsPressedAsState()

    val continueShoppingScale by animateFloatAsState(
        targetValue = if (continueShoppingPressed) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "continue_shopping_scale"
    )

    val viewCartScale by animateFloatAsState(
        targetValue = if (viewCartPressed) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "view_cart_scale"
    )

    AnimatedVisibility(
        visible = shouldAnimate,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(durationMillis = 400, delayMillis = 500)
        ),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Continue Shopping Button - NOW ORANGE
            Button(
                onClick = onDismiss,
                interactionSource = continueShoppingInteractionSource,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = continueShoppingScale
                        scaleY = continueShoppingScale
                    },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary, // Orange background
                    contentColor = MaterialTheme.colorScheme.onTertiary // White text on orange
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    "Continue Shopping",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // View Cart Button - Green primary color
            Button(
                onClick = onConfirm,
                interactionSource = viewCartInteractionSource,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = viewCartScale
                        scaleY = viewCartScale
                    },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Green background
                    contentColor = MaterialTheme.colorScheme.onPrimary // White text on green
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    "View Cart",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
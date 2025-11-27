package com.example.marikitiapp.ui.sharon

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PaymentMethodScreen(
    cartViewModel: CartViewModel,
    paymentViewModel: PaymentViewModel = viewModel { PaymentViewModel(cartViewModel = cartViewModel) },
    onNavigateToSuccess: (String) -> Unit,
    onNavigateToCancel: () -> Unit,
    onBackClick: () -> Unit
) {
    val paymentMethods by paymentViewModel.paymentMethods.collectAsState()
    val selectedMethod by paymentViewModel.selectedPaymentMethod.collectAsState()
    val isProcessing by paymentViewModel.isProcessing.collectAsState()
    val error by paymentViewModel.error.collectAsState()
    val mpesaPhoneNumber by paymentViewModel.mpesaPhoneNumber.collectAsState()
    val totalAmount = paymentViewModel.totalAmount

    // Animation states
    var shouldAnimate by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (shouldAnimate) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "content_alpha"
    )

    LaunchedEffect(Unit) {
        shouldAnimate = true
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = shouldAnimate,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { -50 },
                    animationSpec = tween(durationMillis = 400)
                )
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Payment Method",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(4.dp, CircleShape)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.background(Color.Transparent)
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = shouldAnimate && !isProcessing,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                            clip = true
                        ),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Total Amount with animation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AnimatedContent(
                                targetState = totalAmount,
                                transitionSpec = {
                                    slideInVertically { height -> height } + fadeIn() with
                                            slideOutVertically { height -> -height } + fadeOut()
                                }
                            ) { targetTotal ->
                                Text(
                                    text = "KSh ${String.format("%.2f", targetTotal)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Premium Pay Button
                        AnimatedPayButton(
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    paymentViewModel.processPayment(
                                        onSuccess = { orderId ->
                                            onNavigateToSuccess(orderId)
                                        },
                                        onCancel = onNavigateToCancel
                                    )
                                }
                            },
                            isEnabled = selectedMethod != null && !isProcessing &&
                                    (selectedMethod?.id != "mpesa" || mpesaPhoneNumber.isNotBlank()),
                            isProcessing = isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .graphicsLayer { alpha = contentAlpha }
        ) {
            when {
                isProcessing -> {
                    AnimatedProcessingState()
                }
                else -> {
                    PaymentContent(
                        paymentMethods = paymentMethods,
                        selectedMethod = selectedMethod,
                        onMethodSelect = { paymentViewModel.selectPaymentMethod(it) },
                        mpesaPhoneNumber = mpesaPhoneNumber,
                        onMpesaNumberChange = { paymentViewModel.setMpesaPhoneNumber(it) },
                        error = error,
                        onErrorDismiss = { paymentViewModel.clearError() },
                        shouldAnimate = shouldAnimate
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentContent(
    paymentMethods: List<PaymentMethod>,
    selectedMethod: PaymentMethod?,
    onMethodSelect: (String) -> Unit,
    mpesaPhoneNumber: String,
    onMpesaNumberChange: (String) -> Unit,
    error: String?,
    onErrorDismiss: () -> Unit,
    shouldAnimate: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Payment Methods Header
        item {
            AnimatedVisibility(
                visible = shouldAnimate,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(durationMillis = 400, delayMillis = 100)
                )
            ) {
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Payment Methods
        items(paymentMethods) { method ->
            AnimatedPaymentMethodCard(
                method = method,
                isSelected = method.id == selectedMethod?.id,
                onClick = { onMethodSelect(method.id) },
                shouldAnimate = shouldAnimate,
                index = paymentMethods.indexOf(method)
            )
        }

        // M-Pesa Phone Number Input
        if (selectedMethod?.id == "mpesa") {
            item {
                AnimatedVisibility(
                    visible = shouldAnimate,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = tween(durationMillis = 400, delayMillis = 300)
                    )
                ) {
                    AnimatedMpesaInput(
                        phoneNumber = mpesaPhoneNumber,
                        onPhoneNumberChange = onMpesaNumberChange
                    )
                }
            }
        }

        // Error Message
        if (error != null) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    AnimatedErrorCard(
                        error = error,
                        onDismiss = onErrorDismiss
                    )
                }
            }
        }

        // Bottom spacer for floating action bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedPaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    shouldAnimate: Boolean,
    index: Int
) {
    var isHovered by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )

    AnimatedVisibility(
        visible = shouldAnimate,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(durationMillis = 400, delayMillis = 200 + (index * 100))
        ),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .graphicsLayer { scaleX = cardScale; scaleY = cardScale },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 8.dp else 4.dp
            ),
            border = if (isSelected) {
                BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary
                )
            } else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated Payment Icon
                    AnimatedPaymentIcon(
                        method = method,
                        isSelected = isSelected,
                        modifier = Modifier.size(52.dp)
                    )

                    // Payment Info
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = method.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = method.description, // This should now work with the correct data class
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Selection Indicator with animation
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedPaymentIcon(
    method: PaymentMethod,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "payment_icon")

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        }
                    )
                )
            )
            .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
        contentAlignment = Alignment.Center
    ) {
        // Use appropriate icons based on payment method
        val icon = when (method.id) {
            "mpesa" -> Icons.Default.PhoneAndroid
            "card" -> Icons.Default.CreditCard
            else -> Icons.Default.Wallet
        }

        Icon(
            icon,
            contentDescription = method.name,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun AnimatedMpesaInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "M-Pesa Phone Number",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Fixed TextField - using the correct API
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "254712345678",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                shape = RoundedCornerShape(12.dp),
                // Using the correct way to set colors in Material3
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "Enter your M-Pesa registered phone number in the format: 254XXXXXXXXX",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AnimatedPayButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isHovered && isEnabled) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "pay_button_scale"
    )

    val buttonElevation by animateDpAsState(
        targetValue = if (isHovered && isEnabled) 12.dp else 8.dp,
        animationSpec = tween(durationMillis = 200),
        label = "pay_button_elevation"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = buttonElevation,
            pressedElevation = 16.dp
        ),
        enabled = isEnabled && !isProcessing
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Complete Payment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )

                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Pay",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedProcessingState() {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")

    val dotScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Animated processing indicator
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .graphicsLayer { scaleX = dotScale; scaleY = dotScale },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 4.dp
                )
            }

            Text(
                text = "Processing Payment...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Please wait while we secure your payment",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnimatedErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isHovered) 1.01f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "error_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = cardScale; scaleY = cardScale },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
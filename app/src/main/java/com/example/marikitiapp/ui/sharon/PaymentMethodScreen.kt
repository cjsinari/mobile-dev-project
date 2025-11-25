package com.example.marikitiapp.ui.sharon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    cartViewModel: CartViewModel,
    paymentViewModel: PaymentViewModel = viewModel { PaymentViewModel(cartViewModel = cartViewModel) },
    onNavigateToSuccess: (String) -> Unit, // Pass orderId
    onNavigateToCancel: () -> Unit,
    onBackClick: () -> Unit
) {
    val paymentMethods by paymentViewModel.paymentMethods.collectAsState()
    val selectedMethod by paymentViewModel.selectedPaymentMethod.collectAsState()
    val isProcessing by paymentViewModel.isProcessing.collectAsState()
    val error by paymentViewModel.error.collectAsState()
    val mpesaPhoneNumber by paymentViewModel.mpesaPhoneNumber.collectAsState()
    val totalAmount = paymentViewModel.totalAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Method") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Amount:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Pay Button
                    Button(
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = selectedMethod != null && !isProcessing && 
                            (selectedMethod?.id != "mpesa" || mpesaPhoneNumber.isNotBlank())
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                "Pay Now",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "Processing Payment...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paymentMethods) { method ->
                    PaymentMethodCard(
                        method = method,
                        isSelected = method.id == selectedMethod?.id,
                        onClick = { paymentViewModel.selectPaymentMethod(method.id) }
                    )
                }
                
                // M-Pesa Phone Number Input
                if (selectedMethod?.id == "mpesa") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Enter M-Pesa Phone Number",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                OutlinedTextField(
                                    value = mpesaPhoneNumber,
                                    onValueChange = { paymentViewModel.setMpesaPhoneNumber(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g., 254712345678") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone
                                    )
                                )
                                Text(
                                    text = "Format: 254XXXXXXXXX",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Error Message
                if (error != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = error ?: "An error occurred",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { paymentViewModel.clearError() }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = method.icon,
                        fontSize = 24.sp
                    )
                }

                // Payment Name
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
            }

            // Selection Indicator
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


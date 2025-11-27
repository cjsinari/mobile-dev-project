package com.example.marikitiapp.ui.sharon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.marikitiapp.ui.sharon.data.PaymentRepository
import com.example.marikitiapp.ui.sharon.data.OrderRepository

class PaymentViewModel(
    private val paymentRepository: PaymentRepository = PaymentRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val cartViewModel: CartViewModel
) : ViewModel() {
    // Only M-Pesa and Cash payment methods
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _mpesaPhoneNumber = MutableStateFlow("")
    val mpesaPhoneNumber: StateFlow<String> = _mpesaPhoneNumber.asStateFlow()

    val totalAmount: Double
        get() = cartViewModel.totalPrice

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            // Only M-Pesa and Cash options
            _paymentMethods.value = listOf(
                PaymentMethod(id = "mpesa", name = "M-Pesa", icon = "📱", isSelected = true),
                PaymentMethod(id = "cash", name = "Cash", icon = "💵", isSelected = false)
            )
            _selectedPaymentMethod.value = _paymentMethods.value.first()
        }
    }

    fun selectPaymentMethod(methodId: String) {
        _paymentMethods.value = _paymentMethods.value.map { method ->
            method.copy(isSelected = method.id == methodId)
        }
        _selectedPaymentMethod.value = _paymentMethods.value.find { it.id == methodId }
    }

    fun setMpesaPhoneNumber(phoneNumber: String) {
        _mpesaPhoneNumber.value = phoneNumber
    }

    /**
     * Process payment with escrow logic
     * Payment remains pending until delivery confirmation
     */
    suspend fun processPayment(
        onSuccess: (String) -> Unit, // Returns orderId
        onCancel: () -> Unit
    ) {
        val selectedMethod = _selectedPaymentMethod.value
        if (selectedMethod == null) {
            _error.value = "Please select a payment method"
            onCancel()
            return
        }

        _isProcessing.value = true
        _error.value = null

        try {
            // Create order first
            val orderResult = cartViewModel.createOrder(selectedMethod.id)
            
            if (orderResult.isFailure) {
                _error.value = orderResult.exceptionOrNull()?.message ?: "Failed to create order"
                _isProcessing.value = false
                onCancel()
                return
            }

            val orderId = orderResult.getOrNull() ?: run {
                _error.value = "Failed to get order ID"
                _isProcessing.value = false
                onCancel()
                return
            }

            // Process payment based on method
            val paymentResult = when (selectedMethod.id) {
                "mpesa" -> {
                    val phoneNumber = _mpesaPhoneNumber.value.trim()
                    if (phoneNumber.isEmpty()) {
                        _error.value = "Please enter your M-Pesa phone number"
                        _isProcessing.value = false
                        onCancel()
                        return
                    }
                    paymentRepository.processMpesaPayment(
                        phoneNumber = phoneNumber,
                        amount = totalAmount,
                        orderId = orderId
                    )
                }
                "cash" -> {
                    paymentRepository.processCashPayment(
                        orderId = orderId,
                        amount = totalAmount
                    )
                }
                else -> {
                    Result.failure(Exception("Invalid payment method"))
                }
            }

            _isProcessing.value = false

            if (paymentResult.isSuccess) {
                // Payment is created with "pending" status (escrow)
                // Will be released when delivery is confirmed
                onSuccess(orderId)
            } else {
                _error.value = paymentResult.exceptionOrNull()?.message ?: "Payment processing failed"
                onCancel()
            }
        } catch (e: Exception) {
            _isProcessing.value = false
            _error.value = e.message ?: "An error occurred during payment"
            onCancel()
        }
    }

    fun clearError() {
        _error.value = null
    }
}

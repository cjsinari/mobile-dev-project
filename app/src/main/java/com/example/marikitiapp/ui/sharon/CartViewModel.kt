package com.example.marikitiapp.ui.sharon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.marikitiapp.ui.sharon.data.OrderRepository
import com.example.marikitiapp.ui.sharon.data.AuthRepository

class CartViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val totalPrice: Double
        get() = _cartItems.value.sumOf { it.totalPrice }

    /**
     * Create order from cart items
     */
    suspend fun createOrder(paymentMethod: String): Result<String> {
        if (_cartItems.value.isEmpty()) {
            return Result.failure(Exception("Cart is empty"))
        }

        return try {
            _isLoading.value = true
            _error.value = null

            val buyerId = authRepository.getCurrentUserId()
            val result = orderRepository.createOrder(
                cartItems = _cartItems.value,
                buyerId = buyerId,
                paymentMethod = paymentMethod
            )

            _isLoading.value = false

            if (result.isSuccess) {
                // Clear cart after successful order creation
                _cartItems.value = emptyList()
            }

            result
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = e.message ?: "Failed to create order"
            Result.failure(e)
        }
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(itemId)
        } else {
            _cartItems.value = _cartItems.value.map { item ->
                if (item.product.id == itemId) {
                    item.copy(quantity = newQuantity)
                } else {
                    item
                }
            }
        }
    }

    fun removeItem(itemId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != itemId }
    }

    fun addItem(product: Product, quantity: Int) {
        val existingItem = _cartItems.value.find { it.product.id == product.id }
        _cartItems.value = if (existingItem != null) {
            _cartItems.value.map { item ->
                if (item.product.id == product.id) {
                    item.copy(quantity = item.quantity + quantity)
                } else {
                    item
                }
            }
        } else {
            _cartItems.value + CartItem(product, quantity)
        }
    }

    fun clearError() {
        _error.value = null
    }
}

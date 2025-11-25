package com.example.marikitiapp.ui.sharon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.example.marikitiapp.ui.sharon.data.ProductRepository

class ProductDetailsViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _showAddToCartDialog = MutableStateFlow(false)
    val showAddToCartDialog: StateFlow<Boolean> = _showAddToCartDialog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load product by ID from Firestore with live updates
     */
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            productRepository.getProduct(productId)
                .catch { e ->
                    _error.value = e.message ?: "Failed to load product"
                    _isLoading.value = false
                }
                .collect { result ->
                    result.onSuccess { product ->
                        _product.value = product
                        _isLoading.value = false
                    }.onFailure { e ->
                        _error.value = e.message ?: "Failed to load product"
                        _isLoading.value = false
                    }
                }
        }
    }

    fun increaseQuantity() {
        _quantity.value++
    }

    fun decreaseQuantity() {
        if (_quantity.value > 1) {
            _quantity.value--
        }
    }

    fun showAddToCartDialog() {
        _showAddToCartDialog.value = true
    }

    fun hideAddToCartDialog() {
        _showAddToCartDialog.value = false
    }

    fun clearError() {
        _error.value = null
    }
}

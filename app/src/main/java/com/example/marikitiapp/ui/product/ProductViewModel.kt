package com.example.marikitiapp.ui.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.marikitiapp.data.model.Product
import com.example.marikitiapp.data.repository.FirebaseProductRepository
import com.example.marikitiapp.data.repository.SupabaseUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repo: FirebaseProductRepository) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow<String?>(null) // doc id
    val saveSuccess: StateFlow<String?> = _saveSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun uploadAndSave(
        context: Context,
        name: String,
        price: Double,
        description: String,
        quantity: Int,
        category: String,
        imageBytes: ByteArray
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                val supabaseUploader = SupabaseUploader (
                    supabaseUrl = "https://soyhnrethlkvzvtqpyvv.supabase.co",
                    supabaseApiKey = "sb_publishable_-afeuoWGjgYUmw6h_7p8gg_nh1CpD_Z"
                )

                val imageUrl = supabaseUploader.uploadImageToSupabase(context, imageBytes)
                    ?: throw Exception("Supabase upload failed")

                val product = Product(
                    name = name,
                    price = price,
                    description = description,
                    quantity = quantity,
                    category = category,
                    imageUrl = imageUrl
                )
                val docId = repo.saveProduct(product)
                _saveSuccess.value = docId
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: e.toString()
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSuccess() { _saveSuccess.value = null }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = FirebaseProductRepository()
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repo) as T
        }
    }
}


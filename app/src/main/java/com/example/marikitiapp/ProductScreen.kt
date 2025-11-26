package com.example.marikitiapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

@Composable
fun ProductScreen(
    auth: FirebaseAuth, // FIX 1: Add auth parameter to match the function call
    firestore: FirebaseFirestore
) {
    var productState by remember { mutableStateOf<ProductUiState>(ProductUiState.Loading) }

    // Fetch products
    LaunchedEffect(Unit) {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    productState = ProductUiState.Empty
                } else {
                    val products = snapshot.toObjects<Product>()
                    productState = ProductUiState.Success(products.groupBy { it.category })
                }
            }
            .addOnFailureListener { exception ->
                productState = ProductUiState.Error("Failed to load products: ${exception.message}")
            }
    }

    // FIX 2: Use theme colors for a consistent UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ProductScreenHeader()

        when (val state = productState) {
            is ProductUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is ProductUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProductUiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No products found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is ProductUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    items(state.productsMap.entries.toList()) { (category, products) ->
                        CategorySection(category, products)
                    }
                }
            }
        }
    }
}

sealed class ProductUiState {
    object Loading : ProductUiState()
    object Empty : ProductUiState()
    data class Success(val productsMap: Map<String, List<Product>>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

@Composable
private fun ProductScreenHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MARIKITI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Our Products", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
private fun CategorySection(title: String, products: List<Product>) {
    if (title.isNotBlank()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(product)
            }
        }
    }
}



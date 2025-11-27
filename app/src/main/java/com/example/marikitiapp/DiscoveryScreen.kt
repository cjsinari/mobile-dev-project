package com.example.marikitiapp

// import Product // This import is incorrect and has been removed.

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

@Composable
fun DiscoverScreen(
    firestore: FirebaseFirestore,
    onSignupClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var products by remember { mutableStateOf(listOf<Product>()) }
    var searchText by remember { mutableStateOf("") }

    // Fetch products from Firestore using the efficient toObjects() method
    LaunchedEffect(Unit) {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                // This automatically converts Firestore documents to a list of your Product data class
                products = snapshot.toObjects<Product>()
            }
            .addOnFailureListener {
                // You can add error handling here, e.g., logging or showing a message
            }
    }

    // Filter products based on search text
    val filteredProducts = remember(searchText, products) {
        if (searchText.isBlank()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(searchText, ignoreCase = true) ||
                        product.brand.contains(searchText, ignoreCase = true) ||
                        product.category.contains(searchText, ignoreCase = true)
            }
        }
    }

    Scaffold(
        // Use theme colors for a consistent look
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DiscoverTopBar(
                onLoginClick = onLoginClick,
                onSignupClick = onSignupClick
            )
        }
    ) { paddingValues ->
        // Use a single LazyVerticalGrid for the entire scrollable screen
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from the Scaffold
                .imePadding(),          // Adjust padding for the soft keyboard
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Section: Spans the full width of the grid
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text(
                        text = "Discover",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        // Use a neutral theme color instead of a hardcoded one
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search products...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            // Use theme colors for the text field
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Product Grid Section
            items(filteredProducts) { product ->
                // The ProductCard will correctly use the imageUrl now
                ProductCard(product)
            }
        }
    }
}

@Composable
private fun DiscoverTopBar(
    onSignupClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    // Use the primary theme color (Olive Green) for the top bar
    Surface(color = MaterialTheme.colorScheme.primary) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MARIKITI",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Light
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Home", color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    "Login",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onLoginClick
                    )
                )
                Text(
                    "Sign Up",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSignupClick
                    )
                )
            }
        }
    }
}



package com.example.marikitiapp.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.marikitiapp.data.model.Product
import com.example.marikitiapp.data.repository.FirebaseProductRepository
import com.example.marikitiapp.ui.components.BottomNavigationBar
import com.example.marikitiapp.ui.theme.MarikitiGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel for fetching all products
class YourProductsViewModel(private val repo: FirebaseProductRepository) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch all products for testing (no user filtering)
                _products.value = repo.getAllProducts()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load products"
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = FirebaseProductRepository()
            @Suppress("UNCHECKED_CAST")
            return YourProductsViewModel(repo) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourProductsScreen(
    navController: NavController,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit = {},
    viewModel: YourProductsViewModel = viewModel(factory = YourProductsViewModel.Factory())
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Your Products",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F8F8))
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MarikitiGreen
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Oops! Something went wrong",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProducts() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MarikitiGreen
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                products.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No products yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start posting products to see them here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("post_product") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MarikitiGreen
                            )
                        ) {
                            Text("Post Your First Product")
                        }
                    }
                }

                else -> {
                    ProductGrid(
                        products = products,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<Product>,
    onProductClick: (String) -> Unit
) {
    // Group products by category
    val groupedProducts = products.groupBy { it.category }

    androidx.compose.foundation.lazy.LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        groupedProducts.forEach { (category, categoryProducts) ->
            // Category header
            item {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 16.sp,
                    color = Color(0xFF1F4D2E)
                )
            }

            // Horizontal scrolling row for products
            item {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryProducts) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            modifier = Modifier.width(110.dp) // Fixed width for consistent cards
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        // Product Image with better styling
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Product Name
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            color = Color(0xFF2D2D2D)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Product Price
        Text(
            text = "Ksh ${String.format("%.0f", product.price)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFE76F51),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        // Stock quantity indicator (only show if low stock)
        if (product.quantity < 5 && product.quantity > 0) {
            Text(
                text = "Only ${product.quantity} left",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE76F51),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        } else if (product.quantity == 0) {
            Text(
                text = "Out of stock",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
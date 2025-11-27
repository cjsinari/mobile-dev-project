package com.example.marikitiapp.ui.sharon

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProductDetailsScreen(
    productId: String? = null,
    cartViewModel: CartViewModel,
    viewModel: ProductDetailsViewModel = viewModel(),
    onNavigateToCart: () -> Unit,
    onBackClick: () -> Unit
) {
    val product by viewModel.product.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val showDialog by viewModel.showAddToCartDialog.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        productId?.let { viewModel.loadProduct(it) }
    }

    Scaffold(
        topBar = {
            ProductDetailsTopBar(
                onBackClick = onBackClick,
                isFavorite = isFavorite,
                onFavoriteClick = { isFavorite = !isFavorite },
                onCartClick = onNavigateToCart
            )
        },
        bottomBar = {
            if (!isLoading && error == null && product != null) {
                ProductBottomBar(
                    quantity = quantity,
                    onIncrease = { viewModel.increaseQuantity() },
                    onDecrease = { viewModel.decreaseQuantity() },
                    onAddToCart = { viewModel.showAddToCartDialog() }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> FashionSkeletonLoader()
                error != null -> AnimatedErrorState(error = error, onRetry = {
                    viewModel.clearError()
                    productId?.let { viewModel.loadProduct(it) }
                })
                product != null -> ProductDetailsContent(product!!)
                else -> EmptyProductState()
            }
        }
    }

    // Add to cart dialog
    if (showDialog && product != null) {
        AddToCartPopup(
            product = product!!,
            quantity = quantity,
            cartViewModel = cartViewModel,
            onDismiss = { viewModel.hideAddToCartDialog() },
            onConfirm = {
                viewModel.hideAddToCartDialog()
                onNavigateToCart()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailsTopBar(
    onBackClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCartClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Product Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
        },
        actions = {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onCartClick,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") }
        }
    )
}

@Composable
private fun ProductDetailsContent(product: Product) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ProductImagePager(product = product, pagerState = pagerState)
        ProductInfo(product = product)
    }
}

@Composable
private fun ProductImagePager(product: Product, pagerState: androidx.compose.foundation.pager.PagerState) {
    Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        // Pager indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            repeat(pagerState.pageCount) { index ->
                val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun ProductInfo(product: Product) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(product.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)

        // Rating
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (index < product.rating.toInt()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("${product.rating} (${product.reviewCount} reviews)", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Price
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ksh${product.price}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            product.originalPrice?.let { oldPrice ->
                Spacer(Modifier.width(8.dp))
                Text("Ksh$oldPrice", color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                val discount = ((oldPrice - product.price) / oldPrice * 100).toInt()
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text("-$discount%", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold) }
            }
        }

        // Description
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(product.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun ProductBottomBar(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), clip = true),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModernQuantitySelector(quantity, onDecrease, onIncrease, modifier = Modifier.weight(1f))
            Button(
                onClick = onAddToCart,
                modifier = Modifier.weight(2f).height(56.dp).clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Add to Cart", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun ModernQuantitySelector(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onDecrease,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) { Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) }

        Text(quantity.toString(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

        IconButton(
            onClick = onIncrease,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) { Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) }
    }
}

@Composable
private fun AnimatedErrorState(error: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
        Text(error ?: "Unknown error occurred", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) { Text("Try Again") }
    }
}

@Composable
private fun EmptyProductState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Product not found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FashionSkeletonLoader() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(400.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) {
                Box(modifier = Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
            }
        }
    }
}

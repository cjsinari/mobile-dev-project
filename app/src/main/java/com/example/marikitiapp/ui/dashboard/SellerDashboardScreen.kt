package com.example.marikitiapp.ui.dashboard


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marikitiapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    sellerName: String = "CJ",
    onPostProduct: () -> Unit = {},
    onYourProducts: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold (
        bottomBar = { SellerBottomBar() },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Seller Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    )  { paddingValues ->
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome $sellerName",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Manage your business account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                DashboardCard(
                    title = "Post new product",
                    imageRes = R.drawable.post_product_bg,
                    onClick = onPostProduct
                )
                DashboardCard(
                    title = "Your Products",
                    imageRes = R.drawable.your_products_bg,
                    onClick = onYourProducts
                )
                DashboardCard(
                    title = "Analytics",
                    imageRes = R.drawable.analytics_bg,
                    onClick = onAnalytics
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    imageRes: Int,
    onClick: () -> Unit
)  {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)

    )  {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xAA000000))
                    )
                )
        )

        Text(
            text = title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 16.dp)
        )
    }
}

//Adding bottom navigation
@Composable
fun SellerBottomBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            { /* TODO: Navigate */},
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {/* TODO: Search */},
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {/* TODO: Favourites */},
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Favourites") },
            label = { Text("Profile") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {/* TODO: Profile */},
            { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

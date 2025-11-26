package com.example.marikitiapp

// import Product  <- REMOVE THIS LINE
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProductCard(product: Product) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { /* TODO: Product click action */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(product.imageUrl.ifEmpty { null })
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Text(
                text = product.brand,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
            Text(
                text = "Ksh ${product.price}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}



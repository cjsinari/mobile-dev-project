package com.example.marikitiapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marikitiapp.ui.theme.MarikitiAppTheme

@Composable
fun ProductCard(product: Product) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { /* TODO: Product click action */ },
        shape = RoundedCornerShape(12.dp),
        // FIX: Use theme colors for the card background
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(product.imageUrl.ifEmpty { null })
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    // FIX: Use clip instead of background for shaping
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1, // Keep title to one line for consistency
                overflow = TextOverflow.Ellipsis,
                // FIX: Use theme colors for text
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = product.brand,
                fontSize = 12.sp,
                // FIX: Use a more appropriate theme color for secondary text
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = "Ksh ${product.price}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                // FIX: Use tertiary color (Orange) for the price
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}




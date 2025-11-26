package com.example.marikitiapp.ui.product

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight


@Composable
fun SuccessScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        delay(5000)  // 5-second delay
        navController.navigate("your_products") {
            popUpTo("post_product") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(90.dp),
                tint = Color(0xFFE76F51)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Successfully posted",
                fontSize = 20.sp,
                color = Color(0xFFE76F51),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

package com.example.marikitiapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Luxurious Light Color Scheme with deep, rich tones
private val LightColorScheme = lightColorScheme(
    // Primary - Deep Midnight Forest
    primary = MidnightForest,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0E3),
    onPrimaryContainer = HunterGreen,

    // Secondary - Rich Moss Green
    secondary = MossGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4EDDA),
    onSecondaryContainer = Color(0xFF2F3D26),

    // Tertiary - Warm Rust Orange
    tertiary = RustOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0D6),
    onTertiaryContainer = Color(0xFF6E2C15),

    // Quaternary - Amber Glow for highlights
    // You can use this for CTAs, important actions, badges

    // Surface colors - Warm luxury tones
    background = DeepCream,
    onBackground = CharredBrown,
    surface = Parchment,
    onSurface = CharredBrown,
    surfaceVariant = Color(0xFFF5F1E8),
    onSurfaceVariant = WarmSlate,

    // Interactive elements
    outline = ClayBeige,
    outlineVariant = Color(0xFFE8DFD1),

    // Error colors
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Additional states
    inverseOnSurface = Color(0xFFF5F1E8),
    inverseSurface = Color(0xFF332E25),
    scrim = Color(0xFF000000)
)

// Deep, Rich Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    // Primary - Soft Moss
    primary = Color(0xFFB8CCAA),
    onPrimary = Color(0xFF1A2A1A),
    primaryContainer = ForestShadow,
    onPrimaryContainer = Color(0xFFD4E6C8),

    // Secondary - Warm Clay
    secondary = Color(0xFFD9C8B5),
    onSecondary = Color(0xFF2A231F),
    secondaryContainer = Color(0xFF5D4E41),
    onSecondaryContainer = Color(0xFFF5E6D9),

    // Tertiary - Soft Amber
    tertiary = Color(0xFFFFB59B),
    onTertiary = Color(0xFF5C1B00),
    tertiaryContainer = BurntSienna,
    onTertiaryContainer = Color(0xFFFFDBCF),

    // Surface colors - Deep earthy tones
    background = Color(0xFF14140F),
    onBackground = Color(0xFFE6E2D9),
    surface = Color(0xFF1C1C16),
    onSurface = Color(0xFFE6E2D9),
    surfaceVariant = Color(0xFF2E2E26),
    onSurfaceVariant = Color(0xFFC8C4BA),

    // Interactive elements
    outline = Color(0xFF8A857B),
    outlineVariant = Color(0xFF45483D),

    // Error colors
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Additional states
    inverseOnSurface = Color(0xFF2E2E26),
    inverseSurface = Color(0xFFE6E2D9),
    scrim = Color(0xFF000000)
)

@Composable
fun MarikitiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

            // Optional: Add a subtle warm tint to navigation bar
            window.navigationBarColor = if (darkTheme) {
                Color(0xFF1A1A16).toArgb()
            } else {
                DeepCream.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
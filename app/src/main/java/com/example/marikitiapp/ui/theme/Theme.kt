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

// New Light Color Scheme with updated roles
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E3A22), // Olive Green for Nav Bar
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4F8D5),
    onPrimaryContainer = Color(0xFF151E0C),

    secondary = Color(0xFF596148),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE6C6),
    onSecondaryContainer = Color(0xFF171E09),

    tertiary = Color(0xFFD84315), // Orange for Price/Accents
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF380D00),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFF5F5ED), // Your specified Grey
    onBackground = Color(0xFF1C1C16),
    // FIX: Set surface to a pure white for a brighter look inside components
    surface = Color.White,
    onSurface = Color(0xFF1C1C16),

    // FIX: Use pure white for card containers.
    surfaceVariant = Color.White,
    onSurfaceVariant = Color(0xFF45483D),

    outline = Color(0xFF75786C),
    scrim = Color(0xFF000000)
)

// New Dark Color Scheme with updated roles
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC8DCBA), // Light Olive Green for Dark Theme Nav Bar
    onPrimary = Color(0xFF293420),
    primaryContainer = Color(0xFF2E3A22), // Your Olive Green
    onPrimaryContainer = Color(0xFFE4F8D5),

    secondary = Color(0xFFC1CAAB),
    onSecondary = Color(0xFF2C331D),
    secondaryContainer = Color(0xFF424A32),
    onSecondaryContainer = Color(0xFFDDE6C6),

    tertiary = Color(0xFFFFB59B), // Light Orange for Dark Theme Price/Accents
    onTertiary = Color(0xFF5C1B00),
    tertiaryContainer = Color(0xFFD84315), // Your Orange
    onTertiaryContainer = Color(0xFFFFDBCF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1C1C16),
    onBackground = Color(0xFFE6E2D9),
    surface = Color(0xFF14140F),
    onSurface = Color(0xFFC9C7BF),

    // In dark mode, you want a dark grey for cards, not white.
    // This color is appropriate for dark theme cards.
    surfaceVariant = Color(0xFF31312A),
    onSurfaceVariant = Color(0xFFC5C8B9),

    outline = Color(0xFF8F9285),
    scrim = Color(0xFF000000)
)

@Composable
fun MarikitiAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep this false to use your custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // FIX: Set status bar to be transparent.
            // The color of the content behind it (e.g., the TopAppBar) will be visible.
            window.statusBarColor = Color.Transparent.toArgb()

            // This line correctly handles making the status bar icons (clock, battery) light or dark.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


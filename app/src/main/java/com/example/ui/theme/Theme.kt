package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PolishPrimaryDark,
    secondary = PolishSecondaryDark,
    tertiary = PolishTertiaryDark,
    background = PolishBackgroundDark,
    surface = PolishSurfaceDark,
    surfaceVariant = PolishSurfaceVariantDark,
    outline = PolishOutlineDark,
    onPrimary = PolishSecondary,
    onSecondary = PolishSecondary,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PolishPrimary,
    secondary = PolishSecondary,
    tertiary = PolishTertiary,
    background = PolishBackground,
    surface = PolishSurface,
    surfaceVariant = PolishSurfaceVariant,
    outline = PolishOutline,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = PolishSecondary,
    onSurface = PolishSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce our custom high-end construction theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

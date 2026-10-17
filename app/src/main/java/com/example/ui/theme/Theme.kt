package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOlive,
    onPrimary = Mint,
    primaryContainer = PrimaryOliveLight,
    onPrimaryContainer = MintLight,
    secondary = MediumGreen,
    onSecondary = DarkBg,
    secondaryContainer = PrimaryOliveDark,
    onSecondaryContainer = Mint,
    tertiary = MintInstantDelivery,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkCardSubtle,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOlive,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = DarkGreen,
    secondary = MediumGreen,
    onSecondary = Color.White,
    secondaryContainer = LightFooter,
    onSecondaryContainer = DarkGreen,
    tertiary = DarkGreen,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightCard,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightFooter,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

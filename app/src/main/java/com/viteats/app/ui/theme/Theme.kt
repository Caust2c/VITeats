package com.viteats.app.ui.theme

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

private val NeobrutalLightScheme = lightColorScheme(
    primary = NeobrutalBlack,
    onPrimary = NeobrutalWhite,
    primaryContainer = PastelYellow,
    onPrimaryContainer = NeobrutalBlack,
    secondary = MintGreen,
    onSecondary = NeobrutalBlack,
    secondaryContainer = MintGreen,
    onSecondaryContainer = NeobrutalBlack,
    tertiary = SoftCoral,
    onTertiary = NeobrutalBlack,
    background = LavenderBackground,
    onBackground = NeobrutalBlack,
    surface = NeobrutalWhite,
    onSurface = NeobrutalBlack,
    surfaceVariant = LavenderCard,
    onSurfaceVariant = MutedText
)

private val NeobrutalDarkScheme = darkColorScheme(
    primary = PastelYellow,
    onPrimary = NeobrutalBlack,
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = PastelYellow,
    secondary = MintGreen,
    onSecondary = NeobrutalBlack,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC)
)

@Composable
fun VITeatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NeobrutalDarkScheme
        else -> NeobrutalLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
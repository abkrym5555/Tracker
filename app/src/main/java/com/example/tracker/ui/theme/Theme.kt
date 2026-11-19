package com.example.tracker.ui.theme

import android.app.Activity
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
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object AppColors {
    val NavyDeep = Color(0xFF1E1B4B)

    val Indigo500 = Color(0xFF6366F1)
    val Indigo600 = Color(0xFF4F46E5)
    val IndigoCardStart = Color(0xFF6558F5)
    val IndigoCardEnd = Color(0xFF4F46E5)
    val IndigoSoft = Color(0xFFEEF0FE)
    val PurpleButton = Color(0xFF7C6BF5)

    val Slate900 = Color(0xFF0F172A)
    val Slate500 = Color(0xFF64748B)
    val Slate400 = Color(0xFF94A3B8)
    val Slate300 = Color(0xFFCBD5E1)

    val BgScreen = Color(0xFFF7F8FC)
    val CardStroke = Color(0xFFEEF1F6)

    val Red500 = Color(0xFFEF4444)
    val Red600 = Color(0xFFDC2626)
    val Red200 = Color(0xFFFECACA)
    val Red100 = Color(0xFFFEE2E2)
    val Green500 = Color(0xFF22C55E)

    val TextOnCard = Color(0xFFDDDBFF)
}

private val LightColors = lightColorScheme(
    primary = AppColors.Indigo500,
    onPrimary = Color.White,
    primaryContainer = AppColors.IndigoSoft,
    onPrimaryContainer = AppColors.Indigo600,
    secondary = AppColors.PurpleButton,
    onSecondary = Color.White,
    background = AppColors.BgScreen,
    onBackground = AppColors.Slate900,
    surface = Color.White,
    onSurface = AppColors.Slate900,
    error = AppColors.Red500,
    onError = Color.White,
    outline = AppColors.Slate300
)

@Composable
fun ExpenseTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
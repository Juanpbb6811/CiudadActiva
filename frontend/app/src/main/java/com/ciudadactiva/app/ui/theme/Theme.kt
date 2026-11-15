package com.ciudadactiva.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ciudadactiva.app.util.AppThemeMode
import com.ciudadactiva.app.util.ThemeManager

private val DarkColorScheme = darkColorScheme(
    primary = BotanicalDarkPrimary,
    onPrimary = BotanicalDarkOnPrimary,
    primaryContainer = BotanicalSandGold,
    onPrimaryContainer = Color(0xFF543B18),
    secondary = BotanicalDarkSecondary,
    onSecondary = Color(0xFF113727),
    secondaryContainer = Color(0xFF294E3D),
    onSecondaryContainer = Color(0xFF96BEA8),
    tertiary = BotanicalSageGreen,
    background = BotanicalDarkBackground,
    onBackground = BotanicalDarkTextPrimary,
    surface = BotanicalDarkSurface,
    onSurface = BotanicalDarkTextPrimary,
    surfaceVariant = BotanicalDarkSurfaceVariant,
    onSurfaceVariant = BotanicalDarkTextSecondary,
    outline = BotanicalDarkOutline,
    outlineVariant = Color(0xFF4E453B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF4E161D),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = BotanicalSandGoldDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = BotanicalSandGold,
    onPrimaryContainer = Color(0xFF543B18),
    secondary = BotanicalSageGreen,
    onSecondary = BotanicalForestGreen,
    secondaryContainer = Color(0xFFC3ECD4),
    onSecondaryContainer = Color(0xFF113727),
    tertiary = BotanicalSecondarySlate,
    background = BotanicalLightBackground,
    onBackground = BotanicalLightTextPrimary,
    surface = BotanicalLightSurface,
    onSurface = BotanicalLightTextPrimary,
    surfaceVariant = BotanicalLightSurfaceVariant,
    onSurfaceVariant = BotanicalLightTextSecondary,
    outline = BotanicalLightOutline,
    outlineVariant = Color(0xFFD2C4B7),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun CiudadActivaTheme(
    content: @Composable () -> Unit
) {
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

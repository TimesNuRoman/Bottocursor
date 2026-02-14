package com.cursorai.remote.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CursorDarkColorScheme = darkColorScheme(
    primary = CursorPrimary,
    onPrimary = Color.White,
    primaryContainer = CursorPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = CursorSecondary,
    onSecondary = Color.Black,
    secondaryContainer = CursorSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = CursorSecondary,
    tertiary = CursorTertiary,
    onTertiary = Color.Black,
    tertiaryContainer = CursorTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = CursorTertiary,
    background = CursorSurface,
    onBackground = TextPrimary,
    surface = CursorSurface,
    onSurface = TextPrimary,
    surfaceVariant = CursorSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    outlineVariant = BorderMuted,
    error = StatusError,
    onError = Color.White,
    errorContainer = StatusError.copy(alpha = 0.2f),
    onErrorContainer = StatusError,
    inverseSurface = TextPrimary,
    inverseOnSurface = CursorSurface,
    inversePrimary = CursorPrimaryVariant,
    surfaceTint = CursorPrimary,
)

@Composable
fun CursorAIRemoteTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CursorDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CursorTypography,
        content = content
    )
}

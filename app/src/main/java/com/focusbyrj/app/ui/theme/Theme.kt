/*
 * Copyright (C) 2024-2026 Focus by Rj
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.focusbyrj.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val MidnightColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentViolet,
    tertiary = NeonGreen,
    background = MidnightBlack,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = MidnightBlack,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderGlass
)

val OceanColorScheme = darkColorScheme(
    primary = Color(0xFF0EA5E9),
    secondary = Color(0xFF38BDF8),
    tertiary = Color(0xFF7DD3FC),
    background = Color(0xFF0B1120),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onPrimary = Color(0xFFF8FAFC),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0x33FFFFFF)
)

val SunsetColorScheme = darkColorScheme(
    primary = Color(0xFFF97316),
    secondary = Color(0xFFFB923C),
    tertiary = Color(0xFFFDBA74),
    background = Color(0xFF1A0F0A),
    surface = Color(0xFF331B10),
    surfaceVariant = Color(0xFF4A2B1D),
    onPrimary = Color(0xFFFFF7ED),
    onBackground = Color(0xFFFFF7ED),
    onSurface = Color(0xFFFFF7ED),
    onSurfaceVariant = Color(0xFFFFEDD5),
    outline = Color(0x33FFFFFF)
)

val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    secondary = Color(0xFF34D399),
    tertiary = Color(0xFF6EE7B7),
    background = Color(0xFF051710),
    surface = Color(0xFF0F3628),
    surfaceVariant = Color(0xFF18523D),
    onPrimary = Color(0xFFECFDF5),
    onBackground = Color(0xFFECFDF5),
    onSurface = Color(0xFFECFDF5),
    onSurfaceVariant = Color(0xFFD1FAE5),
    outline = Color(0x33FFFFFF)
)

val MonochromeColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    secondary = Color(0xFFE2E8F0),
    tertiary = Color(0xFFCBD5E1),
    background = Color(0xFF000000),
    surface = Color(0xFF141414),
    surfaceVariant = Color(0xFF262626),
    onPrimary = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFA3A3A3),
    outline = Color(0x33FFFFFF)
)

val LavenderColorScheme = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    secondary = Color(0xFFA78BFA),
    tertiary = Color(0xFFC4B5FD),
    background = Color(0xFF0F0B1A),
    surface = Color(0xFF23193E),
    surfaceVariant = Color(0xFF33255C),
    onPrimary = Color(0xFFF5F3FF),
    onBackground = Color(0xFFF5F3FF),
    onSurface = Color(0xFFF5F3FF),
    onSurfaceVariant = Color(0xFFDDD6FE),
    outline = Color(0x33FFFFFF)
)

@Composable
fun FocusByRjTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("focus_prefs", android.content.Context.MODE_PRIVATE) }
    var appTheme by remember { mutableStateOf(prefs.getString("app_theme", "Midnight") ?: "Midnight") }
    
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "app_theme") {
                appTheme = sharedPreferences.getString(key, "Midnight") ?: "Midnight"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val colorScheme = when (appTheme) {
        "Midnight" -> MidnightColorScheme
        "Ocean" -> OceanColorScheme
        "Sunset" -> SunsetColorScheme
        "Forest" -> ForestColorScheme
        "Monochrome" -> MonochromeColorScheme
        "Lavender" -> LavenderColorScheme
        else -> MidnightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

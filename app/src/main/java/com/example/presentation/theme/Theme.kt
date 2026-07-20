package com.example.presentation.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = Blue500,
    secondary = Orange500,
    tertiary = Emerald500,
    background = Slate900,
    surface = Slate800,
    onPrimary = Slate50,
    onSecondary = Slate900,
    onTertiary = Slate900,
    onBackground = Slate50,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Blue600,
    secondary = Orange500,
    tertiary = Emerald500,
    background = Slate50,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Slate900,
    onSurface = Slate800,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500
  )

@Composable
fun StudyFlowTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color so our sleek theme is applied
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = Shapes, content = content)
}

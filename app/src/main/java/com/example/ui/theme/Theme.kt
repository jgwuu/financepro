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

// Standard Purple (Default)
private val PurpleDarkColorScheme = darkColorScheme(
  primary = Color(0xFFD0BCFF),
  secondary = Color(0xFFCCC2DC),
  tertiary = Color(0xFFEFB8C8)
)

private val PurpleLightColorScheme = lightColorScheme(
  primary = Color(0xFF6650A4),
  secondary = Color(0xFF625B71),
  tertiary = Color(0xFF7D5260)
)

// Forest Emerald (GREEN)
private val GreenDarkColorScheme = darkColorScheme(
  primary = Color(0xFF81C784),
  secondary = Color(0xFFA5D6A7),
  tertiary = Color(0xFFC8E6C9)
)

private val GreenLightColorScheme = lightColorScheme(
  primary = Color(0xFF2E7D32),
  secondary = Color(0xFF4CAF50),
  tertiary = Color(0xFF81C784)
)

// Sunset Coral (ORANGE)
private val OrangeDarkColorScheme = darkColorScheme(
  primary = Color(0xFFFFB74D),
  secondary = Color(0xFFFFCC80),
  tertiary = Color(0xFFFFE0B2)
)

private val OrangeLightColorScheme = lightColorScheme(
  primary = Color(0xFFE65100),
  secondary = Color(0xFFF57C00),
  tertiary = Color(0xFFFFB74D)
)

// Ocean Deep (BLUE)
private val BlueDarkColorScheme = darkColorScheme(
  primary = Color(0xFF90CAF9),
  secondary = Color(0xFF64B5F6),
  tertiary = Color(0xFFBBDEFB)
)

private val BlueLightColorScheme = lightColorScheme(
  primary = Color(0xFF1565C0),
  secondary = Color(0xFF1E88E5),
  tertiary = Color(0xFF90CAF9)
)

// Teal Dream (TEAL)
private val TealDarkColorScheme = darkColorScheme(
  primary = Color(0xFF80CBC4),
  secondary = Color(0xFF4DB6AC),
  tertiary = Color(0xFFB2DFDB)
)

private val TealLightColorScheme = lightColorScheme(
  primary = Color(0xFF00695C),
  secondary = Color(0xFF00897B),
  tertiary = Color(0xFF80CBC4)
)

// Amethyst Rose (ROSE)
private val RoseDarkColorScheme = darkColorScheme(
  primary = Color(0xFFF48FB1),
  secondary = Color(0xFFF06292),
  tertiary = Color(0xFFF8BBD0)
)

private val RoseLightColorScheme = lightColorScheme(
  primary = Color(0xFFC2185B),
  secondary = Color(0xFFD81B60),
  tertiary = Color(0xFFF48FB1)
)

// Gold (GOLD)
private val GoldDarkColorScheme = darkColorScheme(
  primary = Color(0xFFFFD54F),
  secondary = Color(0xFFFFE082),
  tertiary = Color(0xFFFFF59D)
)

private val GoldLightColorScheme = lightColorScheme(
  primary = Color(0xFFFFB300),
  secondary = Color(0xFFFFCA28),
  tertiary = Color(0xFFFFD54F)
)

// Indigo (INDIGO)
private val IndigoDarkColorScheme = darkColorScheme(
  primary = Color(0xFF9FA8DA),
  secondary = Color(0xFFC5CAE9),
  tertiary = Color(0xFFE8EAF6)
)

private val IndigoLightColorScheme = lightColorScheme(
  primary = Color(0xFF283593),
  secondary = Color(0xFF3F51B5),
  tertiary = Color(0xFF9FA8DA)
)

// Crimson (CRIMSON)
private val CrimsonDarkColorScheme = darkColorScheme(
  primary = Color(0xFFEF9A9A),
  secondary = Color(0xFFE57373),
  tertiary = Color(0xFFFFCDD2)
)

private val CrimsonLightColorScheme = lightColorScheme(
  primary = Color(0xFFC62828),
  secondary = Color(0xFFE53935),
  tertiary = Color(0xFFEF9A9A)
)

// Cyan (CYAN)
private val CyanDarkColorScheme = darkColorScheme(
  primary = Color(0xFF80DEEA),
  secondary = Color(0xFF4DD0E1),
  tertiary = Color(0xFFB2EBF2)
)

private val CyanLightColorScheme = lightColorScheme(
  primary = Color(0xFF00838F),
  secondary = Color(0xFF00ACC1),
  tertiary = Color(0xFF80DEEA)
)

// Slate (SLATE)
private val SlateDarkColorScheme = darkColorScheme(
  primary = Color(0xFF90A4AE),
  secondary = Color(0xFFB0BEC5),
  tertiary = Color(0xFFCFD8DC)
)

private val SlateLightColorScheme = lightColorScheme(
  primary = Color(0xFF37474F),
  secondary = Color(0xFF546E7A),
  tertiary = Color(0xFF90A4AE)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  themeAccent: String = "PURPLE",
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeAccent) {
    "GREEN" -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
    "ORANGE" -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
    "BLUE" -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
    "TEAL" -> if (darkTheme) TealDarkColorScheme else TealLightColorScheme
    "ROSE" -> if (darkTheme) RoseDarkColorScheme else RoseLightColorScheme
    "GOLD" -> if (darkTheme) GoldDarkColorScheme else GoldLightColorScheme
    "INDIGO" -> if (darkTheme) IndigoDarkColorScheme else IndigoLightColorScheme
    "CRIMSON" -> if (darkTheme) CrimsonDarkColorScheme else CrimsonLightColorScheme
    "CYAN" -> if (darkTheme) CyanDarkColorScheme else CyanLightColorScheme
    "SLATE" -> if (darkTheme) SlateDarkColorScheme else SlateLightColorScheme
    else -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

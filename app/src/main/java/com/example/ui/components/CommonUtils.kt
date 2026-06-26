package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF2196F3) // Default Blue
    }
}

private var currentCurrencySymbol = "$"

fun setGlobalCurrencySymbol(symbol: String) {
    currentCurrencySymbol = symbol
}

fun formatCurrency(amount: Double): String {
    return "$currentCurrencySymbol${String.format(Locale.US, "%,.2f", amount)}"
}

fun formatInputAmount(input: String): String {
    var cleaned = ""
    var hasDecimalSeparator = false
    for (char in input) {
        if (char.isDigit()) {
            cleaned += char
        } else if ((char == '.' || char == ',') && !hasDecimalSeparator) {
            cleaned += '.'
            hasDecimalSeparator = true
        }
    }
    return cleaned
}

fun formatInitialAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toLong().toString()
    } else {
        amount.toString()
    }
}

class CurrencyVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return androidx.compose.ui.text.input.TransformedText(text, androidx.compose.ui.text.input.OffsetMapping.Identity)
        }
        
        val parts = originalText.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else null
        
        val formattedInteger = try {
            if (integerPart.isNotEmpty()) {
                val longVal = integerPart.toLong()
                String.format(java.util.Locale.US, "%,d", longVal)
            } else {
                ""
            }
        } catch (e: NumberFormatException) {
            integerPart
        }
        
        val formattedText = if (decimalPart != null) {
            "$formattedInteger.$decimalPart"
        } else if (originalText.endsWith(".")) {
            "$formattedInteger."
        } else {
            formattedInteger
        }

        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset <= integerPart.length) {
                    val digitsToRight = integerPart.length - offset
                    val commasTotal = java.lang.Math.max(0, (integerPart.length - 1) / 3)
                    val commasToRight = digitsToRight / 3
                    val commasBeforeCursor = commasTotal - commasToRight
                    return offset + commasBeforeCursor
                } else {
                    val commasTotal = java.lang.Math.max(0, (integerPart.length - 1) / 3)
                    return offset + commasTotal
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                var originalOffset = 0
                var transformedIndex = 0
                while (transformedIndex < offset && transformedIndex < formattedText.length) {
                    if (formattedText[transformedIndex] != ',') {
                        originalOffset++
                    }
                    transformedIndex++
                }
                return originalOffset
            }
        }
        
        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(formattedText), offsetMapping)
    }
}

fun cleanAmountForParsing(input: String): String {
    return input.replace(",", "")
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun getCategoryIcon(name: String): ImageVector {
    return when (name) {
        "Restaurant", "Comida" -> Icons.Default.Restaurant
        "DirectionsBus", "Transporte" -> Icons.Default.DirectionsBus
        "Receipt", "Servicios" -> Icons.Default.Receipt
        "SportsEsports", "Entretenimiento" -> Icons.Default.SportsEsports
        "Home", "Vivienda" -> Icons.Default.Home
        "Category", "Otros" -> Icons.Default.Category
        "ShoppingCart", "Compras" -> Icons.Default.ShoppingCart
        "LocalHospital", "Salud" -> Icons.Default.LocalHospital
        "School", "Educación" -> Icons.Default.School
        "Flight", "Viajes" -> Icons.Default.Flight
        "Work", "Trabajo" -> Icons.Default.Work
        "AttachMoney", "Finanzas" -> Icons.Default.AttachMoney
        "Star" -> Icons.Default.Star
        "Build" -> Icons.Default.Build
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "CardGiftcard" -> Icons.Default.CardGiftcard
        "PhoneAndroid" -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Category
    }
}

val availableIconsList = listOf(
    "Restaurant", "DirectionsBus", "Receipt", "SportsEsports", "Home",
    "ShoppingCart", "LocalHospital", "School", "Flight", "Work",
    "AttachMoney", "Star", "Build", "DirectionsCar", "CardGiftcard", "PhoneAndroid", "Category"
)

val availableColorsList = listOf(
    "#FF9800", // Orange
    "#00BCD4", // Cyan
    "#4CAF50", // Green
    "#E91E63", // Pink
    "#9C27B0", // Purple
    "#2196F3", // Blue
    "#607D8B", // Blue Grey
    "#E91E63", // Red/Pink
    "#FFEB3B", // Yellow
    "#3F51B5", // Indigo
    "#009688", // Teal
    "#795548"  // Brown
)

data class AvatarOption(
    val id: Int,
    val emoji: String,
    val backgroundColorHex: String,
    val name: String
)

val AvatarOptions = listOf(
    AvatarOption(0, "🦊", "#FF7043", "Zorro"),
    AvatarOption(1, "🐨", "#78909C", "Koala"),
    AvatarOption(2, "🦁", "#FFB300", "León"),
    AvatarOption(3, "🐼", "#B0BEC5", "Panda"),
    AvatarOption(4, "🦄", "#AB47BC", "Unicornio"),
    AvatarOption(5, "🚀", "#1E88E5", "Cohete"),
    AvatarOption(6, "💎", "#26A69A", "Diamante"),
    AvatarOption(7, "🦉", "#5D4037", "Búho"),
    AvatarOption(8, "🎨", "#EC407A", "Artista"),
    AvatarOption(9, "🥑", "#66BB6A", "Aguacate"),
    AvatarOption(10, "🦖", "#4CAF50", "Dino"),
    AvatarOption(11, "🐱", "#FFA726", "Gato"),
    AvatarOption(12, "🐶", "#8D6E63", "Perro"),
    AvatarOption(13, "🐉", "#2E7D32", "Dragón"),
    AvatarOption(14, "👾", "#7E57C2", "Arcade"),
    AvatarOption(15, "👑", "#FFD54F", "Corona"),
    AvatarOption(16, "🍕", "#FF8A65", "Pizza"),
    AvatarOption(17, "🍀", "#4DB6AC", "Trébol"),
    AvatarOption(18, "💸", "#81C784", "Dinero")
)

@Composable
fun AvatarImage(
    avatarId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent
) {
    val option = remember(avatarId) {
        AvatarOptions.find { it.id == avatarId } ?: AvatarOptions[0]
    }
    val color = remember(option.backgroundColorHex) {
        Color(android.graphics.Color.parseColor(option.backgroundColorHex))
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .then(
                if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option.emoji,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = (size.value * 0.55).sp
            )
        )
    }
}

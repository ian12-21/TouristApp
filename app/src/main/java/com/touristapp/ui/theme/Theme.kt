package com.touristapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6CA0DC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF253A52),    // dark muted blu
    onPrimaryContainer = Color(0xFFB8D4F0), // light blue text on it
    secondary = Color(0xFF9DA3B0),
    onSecondary = Color(0xFF1C1E2A),
    secondaryContainer = Color(0xFF2F3545),
    background = Color(0xFF1C1E2A),
    surface = Color(0xFF262C3A),
    onBackground = Color(0xFFE8E9ED),
    onSurface = Color(0xFFE8E9ED),
    surfaceVariant = Color(0xFF2F3545),
    onSurfaceVariant = Color(0xFF8A91A0),
    outlineVariant = Color(0xFF3A4155),
    outline = Color(0xFF3A4155),        // for borders on cards/buttons
    error = Color(0xFFFF6B6B),          // for emergency contacts slide, warnings
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF3D2020), // subtle error background
    tertiary = Color(0xFF4ECDC4),       // a teal accent for variety
    onTertiary = Color(0xFF1C1E2A),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TouristAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

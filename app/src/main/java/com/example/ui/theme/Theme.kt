package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QFinDarkColorScheme = darkColorScheme(
    primary = QuantumCyan,
    onPrimary = Color(0xFF00363C),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF82F4FF),
    secondary = BenchmarkGold,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = AlphaGreen,
    onTertiary = Color(0xFF00391A),
    background = TerminalBackground,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TerminalBorder,
    outlineVariant = TerminalBorderGlow,
    error = RiskCrimson,
    onError = Color.White
)

@Composable
fun QFinProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = QFinDarkColorScheme,
        typography = QuantTypography,
        content = content
    )
}

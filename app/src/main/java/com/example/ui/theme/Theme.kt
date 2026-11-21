package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WildcraftColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = ObsidianForest,
    primaryContainer = AmberDark,
    onPrimaryContainer = AmberLight,
    secondary = EmeraldCanopy,
    onSecondary = ObsidianForest,
    secondaryContainer = EmeraldDark,
    onSecondaryContainer = EmeraldLight,
    tertiary = CrystalCyan,
    onTertiary = ObsidianForest,
    tertiaryContainer = CrystalDark,
    onTertiaryContainer = CrystalLight,
    background = ObsidianForest,
    onBackground = CreamText,
    surface = PanelDark,
    onSurface = CreamText,
    surfaceVariant = PanelCard,
    onSurfaceVariant = LichenMuted,
    surfaceContainerLow = PanelDark,
    surfaceContainer = PanelCard,
    surfaceContainerHigh = PanelElevated,
    outline = PanelBorder,
    error = CoralCrimson,
    onError = CreamText,
    errorContainer = CoralContainer,
    onErrorContainer = CreamText
)

private val WildcraftShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun WildcraftTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WildcraftColorScheme,
        typography = WildcraftTypography,
        shapes = WildcraftShapes,
        content = content
    )
}

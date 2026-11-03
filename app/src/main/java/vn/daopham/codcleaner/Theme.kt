package vn.daopham.codcleaner

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class RiskColors(
    val safe: Color, val safeBg: Color,
    val caution: Color, val cautionBg: Color,
    val critical: Color, val criticalBg: Color,
    val neutral: Color, val neutralBg: Color,
    val heroStart: Color, val heroEnd: Color
)

val LocalRisk = staticCompositionLocalOf {
    RiskColors(
        Color(0xFF0E8A73), Color(0xFFDDF3EC),
        Color(0xFFC7741C), Color(0xFFFCEEDC),
        Color(0xFFC93C3C), Color(0xFFFBE3E3),
        Color(0xFF5B6B78), Color(0xFFE9EDF1),
        Color(0xFF0A4F45), Color(0xFF17A387)
    )
}

private val Light = lightColorScheme(
    primary = Color(0xFF0E8A73),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEFE5),
    onPrimaryContainer = Color(0xFF00382E),
    secondary = Color(0xFF4C635C),
    secondaryContainer = Color(0xFFDDE9E4),
    onSecondaryContainer = Color(0xFF1B2E28),
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF15202A),
    surface = Color(0xFFF4F6F8),
    onSurface = Color(0xFF15202A),
    surfaceVariant = Color(0xFFE6EBEE),
    onSurfaceVariant = Color(0xFF5B6B78),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF0F3F5),
    surfaceContainerHighest = Color(0xFFE6EBEE),
    outline = Color(0xFFC5CED4),
    outlineVariant = Color(0xFFE2E7EA),
    error = Color(0xFFC93C3C)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF5CD6B8),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF0B5245),
    onPrimaryContainer = Color(0xFFB8F5E4),
    secondary = Color(0xFFB2CCC3),
    secondaryContainer = Color(0xFF2C3F3A),
    onSecondaryContainer = Color(0xFFDDE9E4),
    background = Color(0xFF0E1417),
    onBackground = Color(0xFFE2E8EB),
    surface = Color(0xFF0E1417),
    onSurface = Color(0xFFE2E8EB),
    surfaceVariant = Color(0xFF263036),
    onSurfaceVariant = Color(0xFF9FB0BA),
    surfaceContainerLowest = Color(0xFF0A0F11),
    surfaceContainerLow = Color(0xFF161D21),
    surfaceContainer = Color(0xFF182025),
    surfaceContainerHigh = Color(0xFF1F282D),
    surfaceContainerHighest = Color(0xFF2A3439),
    outline = Color(0xFF3E4B52),
    outlineVariant = Color(0xFF283237),
    error = Color(0xFFFF8A80)
)

private val DarkRisk = RiskColors(
    Color(0xFF5CD6B8), Color(0xFF12362F),
    Color(0xFFF2B266), Color(0xFF3A2A16),
    Color(0xFFFF8A80), Color(0xFF3D1C1C),
    Color(0xFF9FB0BA), Color(0xFF263036),
    Color(0xFF07332D), Color(0xFF0F7A66)
)

private val AppType = Typography().let { t ->
    t.copy(
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    )
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CodTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    androidx.compose.runtime.CompositionLocalProvider(
        LocalRisk provides if (dark) DarkRisk else LocalRisk.current
    ) {
        MaterialTheme(
            colorScheme = if (dark) Dark else Light,
            typography = AppType,
            shapes = AppShapes,
            content = content
        )
    }
}

@Suppress("unused")
val Mono = TextStyle(fontSize = 12.sp)

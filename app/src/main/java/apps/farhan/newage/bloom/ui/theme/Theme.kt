package apps.farhan.newage.bloom.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val BloomColors = lightColorScheme(
    primary = BloomRose,
    onPrimary = Color.White,
    primaryContainer = BloomRoseLight,
    onPrimaryContainer = BloomRose,
    background = BloomCream,
    onBackground = BloomInk,
    surface = BloomCream,
    onSurface = BloomInk,
    surfaceVariant = BloomRoseLight,
    onSurfaceVariant = BloomMuted,
    outline = BloomOutline,
)

// Serif headings to match the Figma. Swap FontFamily.Serif for your downloaded font if needed.
private val BloomTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
fun BloomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BloomColors,
        typography = BloomTypography,
        content = content,
    )
}
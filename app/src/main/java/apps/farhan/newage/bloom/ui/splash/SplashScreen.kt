import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import apps.farhan.newage.bloom.ui.components.BloomLogo
import androidx.compose.ui.Alignment

@Composable
fun SplashScreen() {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val glow by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700),
        label = "splash_glow",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFF6EAE8).copy(alpha = glow), Color.Transparent),
                        center = center,
                        radius = size.minDimension * 0.8f,
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        BloomLogo(logoSize = 150.dp)
    }
}
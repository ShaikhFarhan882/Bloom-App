package apps.farhan.newage.bloom.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BloomSerifItalic = TextStyle(
    fontFamily = FontFamily.Serif,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.SemiBold,
)

@Composable
fun ScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(bottom = 4.dp)) {
        Text(title, style = BloomSerifItalic.copy(fontSize = 28.sp))
        Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = BloomSerifItalic.copy(fontSize = 16.sp),
        modifier = modifier.padding(top = 8.dp),
    )
}

@Composable
fun BloomCard(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    if (onClick != null) {
        Surface(
            onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape,
            color = color, border = border, content = content,
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(), shape = shape,
            color = color, border = border, content = content,
        )
    }
}

@Composable
fun ChevronIcon() {
    Icon(
        Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp),
    )
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, badgeSize: Dp = 36.dp) {
    Box(
        modifier = modifier.size(badgeSize).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(badgeSize * 0.5f))
    }
}

/** One row used across Dashboard, Insights, Learn and Settings. */
@Composable
fun BloomRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = { ChevronIcon() },
    containerColor: Color = Color.White,
    onClick: (() -> Unit)? = null,
) {
    BloomCard(modifier = modifier, color = containerColor, onClick = onClick) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.let { it(); Spacer(Modifier.width(12.dp)) }
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                subtitle?.let {
                    Text(
                        it, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailing?.let { Spacer(Modifier.width(8.dp)); it() }
        }
    }
}

@Composable
fun ProgressRing(
    fraction: Float,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    ringSize: Dp = 44.dp,
) {
    val animated by animateFloatAsState(targetValue = fraction, label = "ring")
    Box(modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arc = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(color.copy(alpha = 0.45f), 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
            drawArc(color, -90f, 360f * animated, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(ringSize * 0.4f))
    }
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    Surface(onClick = onClick, modifier = modifier, shape = CircleShape, color = container, contentColor = content) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let { Icon(it, null, Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)) }
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit, buttonSize: Dp = 40.dp) {
    Surface(
        onClick = onClick, enabled = enabled, shape = CircleShape, color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.size(buttonSize),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label, fontSize = (buttonSize.value * 0.45f).sp,
                color = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
fun bloomSwitchColors() = SwitchDefaults.colors(
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedThumbColor = Color.White,
)
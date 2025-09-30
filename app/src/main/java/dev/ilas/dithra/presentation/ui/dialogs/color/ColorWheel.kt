package dev.ilas.dithra.presentation.ui.dialogs.color

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Interactive color wheel for HSV color selection
 * 
 * @param hue Current hue value (0-360)
 * @param saturation Current saturation value (0-1)
 * @param value Current value/brightness (0-1)
 * @param onColorChange Callback when hue or saturation changes
 * @param modifier Optional modifier for styling
 */
@Composable
fun ColorWheel(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val areaSize = Size(size.width.toFloat(), size.height.toFloat())
                        handleColorWheelTouch(offset, areaSize, onColorChange)
                    },
                    onDrag = { change, _ ->
                        val areaSize = Size(size.width.toFloat(), size.height.toFloat())
                        handleColorWheelTouch(change.position, areaSize, onColorChange)
                    }
                )
            }
    ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            brush = Brush.sweepGradient(
                listOf(
                    Color.Red,
                    Color.Magenta,
                    Color.Blue,
                    Color.Cyan,
                    Color.Green,
                    Color.Yellow,
                    Color.Red
                )
            ),
            radius = radius,
            center = center
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        if (value < 1f) {
            drawCircle(
                color = Color.Black.copy(alpha = 1f - value),
                radius = radius,
                center = center
            )
        }

        val hueRadians = Math.toRadians(hue.toDouble())
        val indicatorDistance = radius * saturation.coerceIn(0f, 1f)
        val indicatorCenter = Offset(
            x = center.x + (indicatorDistance * cos(hueRadians)).toFloat(),
            y = center.y - (indicatorDistance * sin(hueRadians)).toFloat()
        )

        drawCircle(
            color = Color.White,
            radius = 10.dp.toPx(),
            center = indicatorCenter,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Handle touch input for the color wheel
 */
private fun handleColorWheelTouch(
    offset: Offset,
    size: Size,
    onColorChange: (Float, Float) -> Unit
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val dx = offset.x - centerX
    val dy = offset.y - centerY
    val radius = size.minDimension / 2f
    val distance = sqrt((dx * dx) + (dy * dy)).coerceAtMost(radius)
    val saturation = (distance / radius).coerceIn(0f, 1f)
    val hue = ((Math.toDegrees(atan2(-dy.toDouble(), dx.toDouble())) + 360.0) % 360.0).toFloat()
    onColorChange(hue, saturation)
}
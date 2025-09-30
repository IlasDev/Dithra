package dev.ilas.dithra.presentation.ui.components.controls

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Size control slider for adjusting image processing dimensions.
 * 
 * @param value Current slider value
 * @param onValueChange Callback when value changes
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeSliderSection(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Slider(
            value = value,
            valueRange = 0f..50f,
            steps = 49,
            onValueChange = onValueChange,
            track = { sliderState ->
                SliderDefaults.Track(
                    modifier = Modifier.height(8.dp),
                    sliderState = sliderState,
                    drawTick = { _, _ -> }
                )
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}
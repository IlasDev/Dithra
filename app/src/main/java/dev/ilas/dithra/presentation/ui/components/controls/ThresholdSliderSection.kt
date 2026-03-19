package dev.ilas.dithra.presentation.ui.components.controls

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ilas.dithra.data.model.DitheringMethod
import kotlin.math.roundToInt

/**
 * Threshold control slider with method display and action labels.
 * 
 * @param value Current threshold value
 * @param onValueChange Callback when value changes
 * @param selectedMethod Currently selected dithering method
 * @param modifier Optional modifier for styling
 */
@Composable
fun ThresholdSliderSection(
    value: Float,
    onValueChange: (Float) -> Unit,
    selectedMethod: DitheringMethod,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Method",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Threshold:",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = value.roundToInt().toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = "Palette",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
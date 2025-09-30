@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package dev.ilas.dithra.presentation.ui.components.controls

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdUnits
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.*
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ilas.dithra.data.model.DitheringMethod
import dev.ilas.dithra.data.model.supportsFactor
import dev.ilas.dithra.presentation.ui.utils.NoInteractionSource
import kotlin.math.roundToInt

/**
 * Middle controls section with connected toggle buttons
 * Features method selection, threshold slider, and upload controls in a connected button group
 * 
 * @param value Current threshold value
 * @param onValueChange Callback when threshold value changes
 * @param factorValue Current factor value
 * @param onFactorValueChange Callback when factor value changes
 * @param selectedMethod Currently selected dithering method
 * @param onMethodClick Callback when method button is clicked
 * @param onUploadClick Callback when upload button is clicked
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiddleControlsSection(
    value: Float,
    onValueChange: (Float) -> Unit,
    factorValue: Float,
    onFactorValueChange: (Float) -> Unit,
    selectedMethod: DitheringMethod,
    onMethodClick: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showFactorSlider = selectedMethod.supportsFactor()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val options = listOf("1", "2", "3")
        val unCheckedIcons =
            listOf(Icons.Outlined.GridView, Icons.Outlined.AdUnits, Icons.Outlined.FileUpload)

        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            val modifiers = listOf(Modifier, Modifier.weight(1f), Modifier)

            options.forEachIndexed { index, _ ->
                Button(
                    modifier = modifiers[index]
                        .width(80.dp)
                        .height(60.dp),
                    contentPadding = PaddingValues(16.dp, 0.dp),
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    shape = when (index) {
                        0 -> RoundedCornerShape(
                            topStart = CornerSize(30.dp),
                            topEnd = CornerSize(8.dp),
                            bottomStart = if (showFactorSlider) CornerSize(8.dp) else CornerSize(30.dp),
                            bottomEnd = CornerSize(8.dp)
                        )
                        options.lastIndex -> RoundedCornerShape(
                            topStart = CornerSize(8.dp),
                            topEnd = CornerSize(30.dp),
                            bottomStart = CornerSize(8.dp),
                            bottomEnd = if (showFactorSlider) CornerSize(8.dp) else CornerSize(30.dp)
                        )
                        else -> RoundedCornerShape(
                            topStart = CornerSize(8.dp),
                            topEnd = CornerSize(8.dp),
                            bottomStart = CornerSize(8.dp),
                            bottomEnd = CornerSize(8.dp)
                        )
                    },
                    onClick = {
                        when (index) {
                            0 -> onMethodClick()
                            2 -> onUploadClick()
                        }
                    },
                    interactionSource = if (index == 1) NoInteractionSource() else remember { MutableInteractionSource() },
                ) {
                    if (index == 1) {
                        Slider(
                            value = value,
                            valueRange = 0f..75f,
                            steps = 74,
                            onValueChange = onValueChange,
                            track = { sliderState ->
                                SliderDefaults.Track(
                                    modifier = Modifier.height(24.dp),
                                    sliderState = sliderState,
                                    drawTick = { _, _ -> }
                                )
                            },
                        )
                    } else {
                        Icon(
                            unCheckedIcons[index],
                            contentDescription = "Localized description",
                        )
                    }
                }
            }
        }
        
        if (showFactorSlider) {
            FactorSliderSection(
                factorValue = factorValue,
                onFactorValueChange = onFactorValueChange
            )
        }
    }
}

/**
 * Factor slider section that appears below middle controls when needed
 * 
 * @param factorValue Current factor value
 * @param onFactorValueChange Callback when factor value changes
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FactorSliderSection(
    factorValue: Float,
    onFactorValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp, 8.dp, bottomStart = 30.dp, bottomEnd = 30.dp)
    
    Spacer(modifier = Modifier.height(2.dp))
    Surface(
        modifier = modifier
            .height(60.dp)
            .fillMaxWidth(),
        checked = false,
        onCheckedChange = { },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Slider(
            modifier = Modifier.padding(20.dp, 0.dp),
            value = factorValue,
            valueRange = 0f..30f,
            steps = 29,
            onValueChange = onFactorValueChange,
            track = { sliderState ->
                SliderDefaults.Track(
                    modifier = Modifier.height(24.dp),
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
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Factor:",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = factorValue.roundToInt().toString(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
@file:OptIn(ExperimentalMaterial3Api::class)

package dev.ilas.dithra.presentation.ui.dialogs.color

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.ilas.dithra.R
import dev.ilas.dithra.data.model.Color as PaletteColor

/**
 * Dialog for selecting colors using HSV color wheel and RGB inputs
 * 
 * @param initialColor Initial color to display (null for default red)
 * @param isEditing Whether we're editing an existing color
 * @param onDismiss Callback when dialog is dismissed
 * @param onColorSelected Callback when color is selected
 * @param modifier Optional modifier for styling
 */
@Composable
fun ColorPickerDialog(
    initialColor: PaletteColor?,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onColorSelected: (PaletteColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = initialColor ?: PaletteColor(r = 255, g = 64, b = 64, percentage = 0.0)
    val hsvArray = remember(startColor) {
        FloatArray(3).also { array ->
            android.graphics.Color.RGBToHSV(startColor.r, startColor.g, startColor.b, array)
        }
    }
    var hue by remember { mutableFloatStateOf(hsvArray[0]) }
    var saturation by remember { mutableFloatStateOf(hsvArray[1]) }
    var value by remember { mutableFloatStateOf(hsvArray[2]) }

    val currentColorInt = remember(hue, saturation, value) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation.coerceIn(0f, 1f), value.coerceIn(0f, 1f)))
    }
    val currentComposeColor = remember(currentColorInt) {
        Color(currentColorInt)
    }
    val currentHex = remember(currentColorInt) {
        String.format("%06X", currentColorInt and 0x00FFFFFF)
    }
    var hexField by remember(currentHex) { mutableStateOf(currentHex) }
    var redField by remember(currentColorInt) { mutableStateOf(((currentColorInt shr 16) and 0xFF).toString()) }
    var greenField by remember(currentColorInt) { mutableStateOf(((currentColorInt shr 8) and 0xFF).toString()) }
    var blueField by remember(currentColorInt) { mutableStateOf((currentColorInt and 0xFF).toString()) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(LocalContext.current.getString(R.string.color_picker_title), style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ColorWheel(
                        hue = hue,
                        saturation = saturation,
                        value = value,
                        onColorChange = { newHue, newSaturation ->
                            hue = newHue
                            saturation = newSaturation
                        },
                        modifier = Modifier.size(160.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Slider(
                        value = value,
                        onValueChange = { value = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = currentComposeColor,
                            activeTrackColor = currentComposeColor
                        )
                    )
                }

                OutlinedTextField(
                    value = hexField,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isLetterOrDigit() }.take(6)
                        hexField = filtered.uppercase()
                        if (filtered.length == 6) {
                            PaletteColor.fromHex(filtered)?.let { parsed ->
                                val newHsv = FloatArray(3)
                                android.graphics.Color.RGBToHSV(parsed.r, parsed.g, parsed.b, newHsv)
                                hue = newHsv[0]
                                saturation = newHsv[1]
                                value = newHsv[2]
                                redField = parsed.r.toString()
                                greenField = parsed.g.toString()
                                blueField = parsed.b.toString()
                            }
                        }
                    },
                    label = { Text(LocalContext.current.getString(R.string.color_picker_hex_label)) },
                    singleLine = true,
                    prefix = { Text("#") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RGBField(
                        label = LocalContext.current.getString(R.string.color_picker_r_label),
                        value = redField,
                        onValueChange = { input ->
                            val sanitized = input.filter { it.isDigit() }.take(3)
                            redField = sanitized
                            toIntOrNullWithin(sanitized)?.let { red ->
                                val green = toIntOrNullWithin(greenField) ?: 0
                                val blue = toIntOrNullWithin(blueField) ?: 0
                                val newHsv = FloatArray(3)
                                android.graphics.Color.RGBToHSV(red, green, blue, newHsv)
                                hue = newHsv[0]
                                saturation = newHsv[1]
                                value = newHsv[2]
                                hexField = String.format("%02X%02X%02X", red, green, blue)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    RGBField(
                        label = LocalContext.current.getString(R.string.color_picker_g_label),
                        value = greenField,
                        onValueChange = { input ->
                            val sanitized = input.filter { it.isDigit() }.take(3)
                            greenField = sanitized
                            toIntOrNullWithin(sanitized)?.let { green ->
                                val red = toIntOrNullWithin(redField) ?: 0
                                val blue = toIntOrNullWithin(blueField) ?: 0
                                val newHsv = FloatArray(3)
                                android.graphics.Color.RGBToHSV(red, green, blue, newHsv)
                                hue = newHsv[0]
                                saturation = newHsv[1]
                                value = newHsv[2]
                                hexField = String.format("%02X%02X%02X", red, green, blue)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    RGBField(
                        label = LocalContext.current.getString(R.string.color_picker_b_label),
                        value = blueField,
                        onValueChange = { input ->
                            val sanitized = input.filter { it.isDigit() }.take(3)
                            blueField = sanitized
                            toIntOrNullWithin(sanitized)?.let { blue ->
                                val red = toIntOrNullWithin(redField) ?: 0
                                val green = toIntOrNullWithin(greenField) ?: 0
                                val newHsv = FloatArray(3)
                                android.graphics.Color.RGBToHSV(red, green, blue, newHsv)
                                hue = newHsv[0]
                                saturation = newHsv[1]
                                value = newHsv[2]
                                hexField = String.format("%02X%02X%02X", red, green, blue)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val paletteColor = PaletteColor(
                    r = (currentColorInt shr 16) and 0xFF,
                    g = (currentColorInt shr 8) and 0xFF,
                    b = currentColorInt and 0xFF,
                    percentage = 0.0
                )
                onColorSelected(paletteColor)
            }) {
                Text(if (isEditing) LocalContext.current.getString(R.string.apply) else LocalContext.current.getString(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalContext.current.getString(R.string.cancel))
            }
        }
    )
}

/**
 * RGB input field for color picker
 * 
 * @param label Field label (R, G, or B)
 * @param value Current field value
 * @param onValueChange Callback when value changes
 * @param modifier Optional modifier for styling
 */
@Composable
private fun RGBField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        modifier = modifier
    )
}

/**
 * Utility function to parse int within valid RGB range
 */
private fun toIntOrNullWithin(value: String): Int? {
    val parsed = value.toIntOrNull() ?: return null
    return parsed.coerceIn(0, 255)
}
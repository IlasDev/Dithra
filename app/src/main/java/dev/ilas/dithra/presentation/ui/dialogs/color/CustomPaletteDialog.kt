@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package dev.ilas.dithra.presentation.ui.dialogs.color

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.ilas.dithra.data.model.ColorPalette
import dev.ilas.dithra.data.model.Color as PaletteColor
import dev.ilas.dithra.R
import androidx.compose.ui.platform.LocalContext
import dev.ilas.dithra.data.model.Category

/**
 * Dialog for creating or editing custom color palettes
 * 
 * @param initialPalette Existing palette to edit (null for new palette)
 * @param onDismiss Callback when dialog is dismissed
 * @param onSave Callback when palette is saved
 * @param onRequestImportData Callback to get clipboard data for import
 * @param parseExportedPalette Callback to parse exported palette string
 * @param onShowToast Callback to show toast messages
 * @param modifier Optional modifier for styling
 */
@Composable
fun CustomPaletteDialog(
    initialPalette: ColorPalette?,
    onDismiss: () -> Unit,
    onSave: (ColorPalette) -> Unit,
    onRequestImportData: () -> String?,
    parseExportedPalette: (String) -> ColorPalette?,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember(initialPalette?.id) { mutableStateOf(initialPalette?.name ?: "") }
    val paletteColors = remember(initialPalette?.id) {
        mutableStateListOf<PaletteColor>().apply {
            initialPalette?.colors?.let { addAll(it) }
        }
    }
    var showColorPicker by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var pickerInitialColor by remember { mutableStateOf<PaletteColor?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val canAddMore = paletteColors.size < ColorPalette.MAX_COLORS

    fun openColorPicker(index: Int?) {
        editingIndex = index
        pickerInitialColor = index?.let { paletteColors.getOrNull(it) }
        showColorPicker = true
    }

    fun persistColor(color: PaletteColor) {
        if (editingIndex != null && editingIndex!! in paletteColors.indices) {
            paletteColors[editingIndex!!] = color
        } else if (paletteColors.size < ColorPalette.MAX_COLORS) {
            paletteColors.add(color)
        }
        showColorPicker = false
        editingIndex = null
        errorMessage = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = if (initialPalette == null) LocalContext.current.getString(R.string.palette_new) else LocalContext.current.getString(R.string.palette_edit),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(40)
                    },
                    label = { Text(LocalContext.current.getString(R.string.palette_name_label)) },
                    singleLine = true,
                    supportingText = {
                        Text(text = "${name.length}/40")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = LocalContext.current.getString(R.string.palette_colors_count, paletteColors.size, ColorPalette.MAX_COLORS),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            paletteColors.forEachIndexed { index, color ->
                                ColorSwatch(
                                    color = Color(color.toColorInt()),
                                    onClick = { openColorPicker(index) },
                                    onRemove = {
                                        paletteColors.removeAt(index)
                                        errorMessage = null
                                    }
                                )
                            }
                            ColorAddButton(
                                enabled = canAddMore,
                                onClick = {
                                    if (canAddMore) {
                                        openColorPicker(null)
                                    } else {
                                        errorMessage = context.getString(R.string.palette_max_colors_reached, ColorPalette.MAX_COLORS)
                                    }
                                }
                            )
                        }
                    }

                    Text(
                        text = LocalContext.current.getString(R.string.palette_color_edit_instruction),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AssistChip(
                        onClick = {
                            val raw = onRequestImportData()
                            if (raw.isNullOrBlank()) {
                                onShowToast(context.getString(R.string.error_clipboard_empty))
                            } else {
                                val imported = parseExportedPalette(raw)
                                if (imported == null) {
                                    onShowToast(context.getString(R.string.error_clipboard_invalid_palette))
                                } else if (imported.name.length > 40) {
                                    onShowToast(context.getString(R.string.error_palette_name_too_long))
                                } else {
                                    name = imported.name
                                    paletteColors.clear()
                                    paletteColors.addAll(imported.colors)
                                    errorMessage = null
                                }
                            }
                        },
                        label = { Text(context.getString(R.string.palette_import_clipboard)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.FileUpload,
                                contentDescription = null
                            )
                        }
                    )
                }

                errorMessage?.let { message ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = paletteColors.isNotEmpty(),
                onClick = {
                    if (paletteColors.isEmpty()) {
                        errorMessage = context.getString(R.string.palette_add_one_color)
                        return@TextButton
                    }
                    val evenPercentage = 100.0 / paletteColors.size
                    val sanitizedColors = paletteColors.map { color ->
                        color.copy(percentage = evenPercentage)
                    }
                    val palette = ColorPalette(
                        category = Category.CUSTOM,
                        name = name.ifBlank { context.getString(R.string.palette_untitled) },
                        colors = sanitizedColors,
                        colorCount = sanitizedColors.size,
                        id = initialPalette?.id ?: java.util.UUID.randomUUID().toString(),
                        isCustom = true
                    )
                    onSave(palette)
                }
            ) {
                Text(context.getString(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.cancel))
            }
        }
    )

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = pickerInitialColor,
            isEditing = editingIndex != null,
            onDismiss = {
                showColorPicker = false
                editingIndex = null
            },
            onColorSelected = { selected ->
                persistColor(selected)
            }
        )
    }
}
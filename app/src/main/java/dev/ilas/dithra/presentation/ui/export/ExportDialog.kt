package dev.ilas.dithra.presentation.ui.export

import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.ilas.dithra.R
import dev.ilas.dithra.data.model.ExportSettings
import dev.ilas.dithra.presentation.ui.dialogs.DialogCategory
import dev.ilas.dithra.presentation.ui.dialogs.DialogDropdownRow
import dev.ilas.dithra.presentation.ui.dialogs.DialogScrollColumn
import dev.ilas.dithra.presentation.ui.dialogs.DialogToggleRow

/**
 * Image export configuration dialog.
 *
 * @param processedBitmap Source bitmap for export
 * @param pixelDimensions Actual pixel dimensions for sizing calculations
 * @param initialSettings Persisted export preferences
 * @param onDismiss Called when dialog is dismissed
 * @param onExport Called when export is confirmed with selected settings
 * @param modifier Optional modifier for styling
 */
@Composable
fun ExportDialog(
    processedBitmap: Bitmap?,
    pixelDimensions: Pair<Int, Int>?,
    initialSettings: ExportSettings,
    onDismiss: () -> Unit,
    onExport: (exportType: ExportType, resolution: ExportResolution, transparentWhite: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedExportType by remember { mutableStateOf(initialSettings.exportType) }
    var selectedResolution by remember { mutableStateOf(initialSettings.pngResolution) }
    var transparentWhite by remember { mutableStateOf(initialSettings.transparentWhite) }

    val effectiveResolution = if (selectedExportType == ExportType.SVG) {
        ExportResolution.PIXEL_SIZE
    } else {
        selectedResolution
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = context.getString(R.string.export_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            DialogScrollColumn {
                DialogCategory(
                    title = context.getString(R.string.export_format_title),
                    icon = Icons.Outlined.Image
                ) {
                    DialogDropdownRow(
                        label = context.getString(R.string.export_type_label),
                        selectedValue = selectedExportType.getDisplayName(context),
                        options = ExportType.entries.map { it.getDisplayName(context) },
                        onSelectionChanged = { displayName ->
                            val exportType = ExportType.entries.find { it.getDisplayName(context) == displayName }
                            exportType?.let { type ->
                                selectedExportType = type
                                selectedResolution = if (type == ExportType.SVG) {
                                    ExportResolution.PIXEL_SIZE
                                } else {
                                    initialSettings.pngResolution
                                }
                            }
                        },
                        index = 0,
                        totalItems = 2
                    )

                    val (resolutionDisplay, resolutionOptions) = when (selectedExportType) {
                        ExportType.SVG -> {
                            val display = pixelDimensions?.let { (_, _) ->
                                context.getString(R.string.export_resolution_actual)
                            } ?: context.getString(R.string.export_resolution_actual)
                            display to emptyList()
                        }
                        else -> {
                            val display = selectedResolution.getDisplayName(context, processedBitmap, pixelDimensions)
                            val options = ExportResolution.entries.map {
                                it.getDisplayName(context, processedBitmap, pixelDimensions)
                            }
                            display to options
                        }
                    }

                    DialogDropdownRow(
                        label = context.getString(R.string.export_resolution_label),
                        selectedValue = resolutionDisplay,
                        options = resolutionOptions,
                        onSelectionChanged = { option ->
                            ExportResolution.entries
                                .find { it.getDisplayName(context, processedBitmap, pixelDimensions) == option }
                                ?.let { selectedResolution = it }
                        },
                        index = 1,
                        totalItems = 2,
                        enabled = selectedExportType != ExportType.SVG
                    )
                }

                DialogCategory(
                    title = context.getString(R.string.export_options_title),
                    icon = Icons.Outlined.Settings
                ) {
                    DialogToggleRow(
                        label = context.getString(R.string.export_transparent_white_label),
                        description = context.getString(R.string.export_transparent_white_description),
                        checked = transparentWhite,
                        onCheckedChange = { transparentWhite = it },
                        index = 0,
                        totalItems = 1
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onExport(selectedExportType, effectiveResolution, transparentWhite)
                }
            ) {
                Text(context.getString(R.string.export))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.cancel))
            }
        }
    )
}
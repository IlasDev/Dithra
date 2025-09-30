package dev.ilas.dithra.data.model

import dev.ilas.dithra.presentation.ui.export.ExportType
import dev.ilas.dithra.presentation.ui.export.ExportResolution

/**
 * Export configuration settings persisted between app sessions.
 *
 * @param exportType Output file format
 * @param pngResolution Export resolution setting
 * @param transparentWhite Whether to make white pixels transparent
 */
data class ExportSettings(
    val exportType: ExportType = ExportType.PNG,
    val pngResolution: ExportResolution = ExportResolution.HD_1920,
    val transparentWhite: Boolean = false
) {
    companion object {
        val DEFAULT = ExportSettings()
    }
}
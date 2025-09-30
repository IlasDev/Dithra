package dev.ilas.dithra.data.repository

import android.content.Context
import android.content.SharedPreferences
import dev.ilas.dithra.data.model.ExportSettings
import dev.ilas.dithra.presentation.ui.export.ExportType
import dev.ilas.dithra.presentation.ui.export.ExportResolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

/**
 * Repository for managing export settings and preferences using SharedPreferences.
 */
class ExportRepository(context: Context) {
    
    companion object {
        private const val EXPORT_PREFS_NAME = "dithra_export_settings"
        private const val KEY_EXPORT_TYPE = "export_type"
        private const val KEY_PNG_RESOLUTION = "png_resolution"
        private const val KEY_TRANSPARENT_WHITE = "transparent_white"
    }
    
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences(EXPORT_PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _exportSettings = MutableStateFlow(loadExportSettings())
    val exportSettings: StateFlow<ExportSettings> = _exportSettings.asStateFlow()
    
    /**
     * Loads export settings from SharedPreferences.
     */
    private fun loadExportSettings(): ExportSettings {
        val exportTypeString = sharedPrefs.getString(KEY_EXPORT_TYPE, ExportType.PNG.name)
        val exportType = ExportType.entries.find { it.name == exportTypeString } ?: ExportType.PNG
        
        val pngResolutionString = sharedPrefs.getString(KEY_PNG_RESOLUTION, ExportResolution.HD_1920.name)
        val pngResolution = ExportResolution.entries.find { it.name == pngResolutionString } ?: ExportResolution.HD_1920
        
        val transparentWhite = sharedPrefs.getBoolean(KEY_TRANSPARENT_WHITE, false)
        
        return ExportSettings(
            exportType = exportType,
            pngResolution = pngResolution,
            transparentWhite = transparentWhite
        )
    }
    
    /**
     * Saves export settings to SharedPreferences.
     */
    private fun saveExportSettings(settings: ExportSettings) {
        sharedPrefs.edit {
            putString(KEY_EXPORT_TYPE, settings.exportType.name)
                .putString(KEY_PNG_RESOLUTION, settings.pngResolution.name)
                .putBoolean(KEY_TRANSPARENT_WHITE, settings.transparentWhite)
        }
    }
    
    /**
     * Updates export type setting.
     */
    fun updateExportType(exportType: ExportType) {
        val newSettings = _exportSettings.value.copy(exportType = exportType)
        _exportSettings.value = newSettings
        saveExportSettings(newSettings)
    }
    
    /**
     * Updates resolution setting.
     */
    fun updatePngResolution(resolution: ExportResolution) {
        val newSettings = _exportSettings.value.copy(pngResolution = resolution)
        _exportSettings.value = newSettings
        saveExportSettings(newSettings)
    }
    
    /**
     * Updates transparent white setting.
     */
    fun updateTransparentWhite(enabled: Boolean) {
        val newSettings = _exportSettings.value.copy(transparentWhite = enabled)
        _exportSettings.value = newSettings
        saveExportSettings(newSettings)
    }
    
    /**
     * Updates all export settings at once.
     * Used when user confirms export with new settings.
     */
    fun updateAllSettings(exportType: ExportType, pngResolution: ExportResolution, transparentWhite: Boolean) {
        val newSettings = ExportSettings(
            exportType = exportType,
            pngResolution = pngResolution,
            transparentWhite = transparentWhite
        )
        _exportSettings.value = newSettings
        saveExportSettings(newSettings)
    }
    
    /**
     * Resets export settings to default values.
     */
    fun resetToDefaults() {
        _exportSettings.value = ExportSettings.DEFAULT
        saveExportSettings(ExportSettings.DEFAULT)
    }
}
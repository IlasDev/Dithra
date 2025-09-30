package dev.ilas.dithra.data.repository

import android.content.Context
import android.content.SharedPreferences
import dev.ilas.dithra.data.model.AppSettings
import dev.ilas.dithra.data.model.ThemeMode
import dev.ilas.dithra.data.model.TransparencyMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

/**
 * Repository for managing app settings and preferences using SharedPreferences.
 */
class SettingsRepository(context: Context) {
    
    companion object {
        private const val SETTINGS_PREFS_NAME = "dithra_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLORS = "dynamic_colors"
        private const val KEY_TRANSPARENCY_MODE = "transparency_mode"
    }
    
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    /**
     * Loads settings from SharedPreferences.
     */
    private fun loadSettings(): AppSettings {
        val themeModeString = sharedPrefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val themeMode = ThemeMode.entries.find { it.name == themeModeString } ?: ThemeMode.SYSTEM
        val dynamicColors = sharedPrefs.getBoolean(KEY_DYNAMIC_COLORS, false)
        val transparencyModeString = sharedPrefs.getString(KEY_TRANSPARENCY_MODE, TransparencyMode.BLACK.name)
        val transparencyMode = TransparencyMode.entries.find { it.name == transparencyModeString } ?: TransparencyMode.BLACK
        
        return AppSettings(
            themeMode = themeMode,
            dynamicColors = dynamicColors,
            transparencyMode = transparencyMode
        )
    }
    
    /**
     * Saves settings to SharedPreferences.
     */
    private fun saveSettings(settings: AppSettings) {
        sharedPrefs.edit {
            putString(KEY_THEME_MODE, settings.themeMode.name)
                .putBoolean(KEY_DYNAMIC_COLORS, settings.dynamicColors)
                .putString(KEY_TRANSPARENCY_MODE, settings.transparencyMode.name)
        }
    }
    
    /**
     * Updates theme mode setting.
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        val newSettings = _settings.value.copy(themeMode = themeMode)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * Updates dynamic colors setting.
     */
    fun updateDynamicColors(enabled: Boolean) {
        val newSettings = _settings.value.copy(dynamicColors = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * Updates transparency mode setting.
     */
    fun updateTransparencyMode(transparencyMode: TransparencyMode) {
        val newSettings = _settings.value.copy(transparencyMode = transparencyMode)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * Resets all settings to default values.
     */
    fun resetToDefaults() {
        _settings.value = AppSettings.DEFAULT
        saveSettings(AppSettings.DEFAULT)
    }
}
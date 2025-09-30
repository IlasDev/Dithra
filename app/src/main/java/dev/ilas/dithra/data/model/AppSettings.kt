package dev.ilas.dithra.data.model

import android.content.Context
import dev.ilas.dithra.R

/**
 * Enum representing different theme modes available in the app
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;
    
    /**
     * Gets the display name for this theme mode.
     * 
     * @param context Context for accessing string resources
     * @return Localized display name
     */
    fun getDisplayName(context: Context): String {
        return when (this) {
            SYSTEM -> context.getString(R.string.theme_system)
            LIGHT -> context.getString(R.string.theme_light)
            DARK -> context.getString(R.string.theme_dark)
        }
    }
}

/**
 * Enum representing how transparent pixels should be handled in PNG images
 */
enum class TransparencyMode {
    BLACK,
    WHITE;
    
    /**
     * Gets the display name for this transparency mode.
     * 
     * @param context Context for accessing string resources
     * @return Localized display name
     */
    fun getDisplayName(context: Context): String {
        return when (this) {
            BLACK -> context.getString(R.string.transparency_black)
            WHITE -> context.getString(R.string.transparency_white)
        }
    }
}

/**
 * Data class representing all app settings and user preferences
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = false,
    val transparencyMode: TransparencyMode = TransparencyMode.BLACK
) {
    companion object {
        /**
         * Default settings with fallback values
         */
        val DEFAULT = AppSettings()
    }
}
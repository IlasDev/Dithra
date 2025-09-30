package dev.ilas.dithra.data.model

import android.content.Context
import dev.ilas.dithra.R

/**
 * Represents a color with RGB values and usage percentage.
 *
 * @param r Red component (0-255)
 * @param g Green component (0-255)
 * @param b Blue component (0-255)

    val GAMEBOY_GREEN = ColorPalette()
 * @param percentage Usage percentage in palette
 */
data class Color(
    val r: Int,
    val g: Int,
    val b: Int,
    val percentage: Double
) {
    /**
     * Converts color to hex string representation.
     */
    fun toHex(): String = "#%02X%02X%02X".format(r, g, b)

    /**
     * Converts color to Android Color integer.
     */
    fun toColorInt(): Int = android.graphics.Color.rgb(r, g, b)

    companion object {
        /**
         * Creates Color from hex string.
         *
         * @param value Hex color string (with or without #)
         * @param percentage Usage percentage
         * @return Color instance or null if invalid
         */
        fun fromHex(value: String, percentage: Double = 0.0): Color? {
            val cleaned = value.trim().removePrefix("#")
            if (cleaned.length != 6) return null
            return runCatching {
                val r = cleaned.substring(0, 2).toInt(16)
                val g = cleaned.substring(2, 4).toInt(16)
                val b = cleaned.substring(4, 6).toInt(16)
                Color(r = r, g = g, b = b, percentage = percentage)
            }.getOrNull()
        }
    }
}

/**
 * Represents a category for color palettes.
 *
 * @param id Unique identifier for the category
 * @param nameResId String resource ID for the category name
 */
data class PaletteCategory(
    val id: Int,
    val nameResId: Int
)

object Category {
    val CUSTOM = PaletteCategory(0, R.string.palette_category_custom)
    val CLASSIC_COMPUTER = PaletteCategory(1, R.string.palette_category_classic_computer)
    val HANDHELD = PaletteCategory(2, R.string.palette_category_handheld)
    val MODERN = PaletteCategory(3, R.string.palette_category_modern)
    val SYNTHWAVE = PaletteCategory(4, R.string.palette_category_synthwave)
    val RETRO = PaletteCategory(5, R.string.palette_category_retro)
    val ATMOSPHERIC = PaletteCategory(6, R.string.palette_category_atmospheric)

    /**
     * Returns all predefined categories.
     */
    fun getAllCategories(): List<PaletteCategory> = listOf(
        CUSTOM,
        CLASSIC_COMPUTER,
        HANDHELD,
        MODERN,
        SYNTHWAVE,
        RETRO,
        ATMOSPHERIC
    )
}

/**
 * Represents a color palette with metadata and color definitions.
 *
 * @param category Palette category (e.g., "Classic Computer", "Modern")
 * @param name Display name of the palette
 * @param colors List of colors in the palette
 * @param colorCount Number of colors (must match colors.size)
 * @param id Unique identifier for the palette
 * @param isCustom Whether this is a user-created palette
 */
data class ColorPalette(
    val category: PaletteCategory,
    val name: String,
    val colors: List<Color>,
    val colorCount: Int = colors.size,
    val id: String = "$category:$name",
    val isCustom: Boolean = false
) {
    init {
        require(colorCount == colors.size) {
            "colorCount must match the number of colors provided" // Internal validation message
        }
    }

    /**
     * Creates a copy with evenly distributed color percentages.
     */
    fun updatePercentagesEvenly(): ColorPalette {
        if (colors.isEmpty()) return this
        val splitPercentage = 100.0 / colors.size
        val normalized = colors.map { it.copy(percentage = splitPercentage) }
        return copy(colors = normalized, colorCount = normalized.size)
    }

    /**
     * Exports palette to string format for sharing.
     */
    fun toExportString(): String {
        val normalized = updatePercentagesEvenly()
        val colorHexes = normalized.colors.joinToString(separator = ",") { color ->
            color.toHex().removePrefix("#")
        }
        val safeName = normalized.name.replace("|", "︱")
        return "${EXPORT_PREFIX}$safeName|$colorHexes"
    }

    companion object {
        const val MAX_COLORS = 128
        private const val EXPORT_PREFIX = "DithraPalette:v1|"

        /**
         * Creates palette from exported string format.
         *
         * @param context Context for accessing string resources
         * @param raw Exported palette string
         * @return ColorPalette instance or null if invalid
         */
        fun fromExportString(context: Context, raw: String): ColorPalette? {
            if (!raw.startsWith(EXPORT_PREFIX)) return null
            val payload = raw.removePrefix(EXPORT_PREFIX)
            val parts = payload.split("|")
            if (parts.size != 2) return null
            val name = parts[0].ifBlank { context.getString(R.string.placeholder_imported_palette) }
            val hexValues = parts[1].split(",").mapNotNull { token ->
                if (token.isBlank()) null else Color.fromHex(token)
            }
            if (hexValues.isEmpty()) return null
            val trimmed = hexValues.take(MAX_COLORS)
            val splitPercentage = 100.0 / trimmed.size
            val colorsWithPercentage = trimmed.map { color ->
                color.copy(percentage = splitPercentage)
            }
            return ColorPalette(
                category = Category.CUSTOM,
                name = name,
                colors = colorsWithPercentage,
                colorCount = colorsWithPercentage.size,
                id = java.util.UUID.randomUUID().toString(),
                isCustom = true
            )
        }
    }
}

/**
 * Predefined color palettes organized by category and era.
 */
object Palette {
    val COMMODORE_64 = ColorPalette(
        category = Category.CLASSIC_COMPUTER,
        name = "Commodore 64",
        colorCount = 16,
        colors = listOf(
            Color(r = 0, g = 0, b = 0, percentage = 8.0),
            Color(r = 255, g = 255, b = 255, percentage = 8.0),
            Color(r = 136, g = 57, b = 50, percentage = 6.0),
            Color(r = 103, g = 182, b = 189, percentage = 6.0),
            Color(r = 139, g = 63, b = 150, percentage = 6.0),
            Color(r = 85, g = 160, b = 73, percentage = 6.0),
            Color(r = 64, g = 49, b = 141, percentage = 6.0),
            Color(r = 191, g = 206, b = 114, percentage = 6.0),
            Color(r = 139, g = 84, b = 41, percentage = 6.0),
            Color(r = 87, g = 66, b = 0, percentage = 6.0),
            Color(r = 184, g = 105, b = 98, percentage = 6.0),
            Color(r = 80, g = 80, b = 80, percentage = 6.0),
            Color(r = 120, g = 120, b = 120, percentage = 6.0),
            Color(r = 148, g = 224, b = 137, percentage = 6.0),
            Color(r = 120, g = 105, b = 196, percentage = 6.0),
            Color(r = 159, g = 159, b = 159, percentage = 6.0)
        )
    )

    val APPLE_II = ColorPalette(
        category = Category.CLASSIC_COMPUTER,
        name = "Apple II",
        colorCount = 6,
        colors = listOf(
            Color(r = 0, g = 0, b = 0, percentage = 20.0),
            Color(r = 227, g = 30, b = 96, percentage = 16.0),
            Color(r = 96, g = 78, b = 189, percentage = 16.0),
            Color(r = 255, g = 255, b = 255, percentage = 16.0),
            Color(r = 20, g = 245, b = 60, percentage = 16.0),
            Color(r = 255, g = 106, b = 60, percentage = 16.0)
        )
    )

    val TELETEXT = ColorPalette(
        category = Category.CLASSIC_COMPUTER,
        name = "Teletext",
        colorCount = 8,
        colors = listOf(
            Color(r = 0, g = 0, b = 0, percentage = 14.0),
            Color(r = 255, g = 0, b = 0, percentage = 14.0),
            Color(r = 0, g = 255, b = 0, percentage = 14.0),
            Color(r = 255, g = 255, b = 0, percentage = 14.0),
            Color(r = 0, g = 0, b = 255, percentage = 14.0),
            Color(r = 255, g = 0, b = 255, percentage = 14.0),
            Color(r = 0, g = 255, b = 255, percentage = 14.0),
            Color(r = 255, g = 255, b = 255, percentage = 16.0)
        )
    )

    val ZX_SPECTRUM = ColorPalette(
        category = Category.CLASSIC_COMPUTER,
        name = "ZX Spectrum",
        colorCount = 15,
        colors = listOf(
            Color(r = 0, g = 0, b = 0, percentage = 8.0),
            Color(r = 0, g = 0, b = 215, percentage = 7.0),
            Color(r = 215, g = 0, b = 0, percentage = 7.0),
            Color(r = 215, g = 0, b = 215, percentage = 7.0),
            Color(r = 0, g = 215, b = 0, percentage = 7.0),
            Color(r = 0, g = 215, b = 215, percentage = 7.0),
            Color(r = 215, g = 215, b = 0, percentage = 7.0),
            Color(r = 215, g = 215, b = 215, percentage = 7.0),
            Color(r = 0, g = 0, b = 255, percentage = 7.0),
            Color(r = 255, g = 0, b = 0, percentage = 7.0),
            Color(r = 255, g = 0, b = 255, percentage = 7.0),
            Color(r = 0, g = 255, b = 0, percentage = 7.0),
            Color(r = 0, g = 255, b = 255, percentage = 7.0),
            Color(r = 255, g = 255, b = 0, percentage = 7.0),
            Color(r = 255, g = 255, b = 255, percentage = 7.0)
        )
    )


    val GAME_BOY_GREEN = ColorPalette(
        category = Category.HANDHELD,
        name = "Game Boy",
        colorCount = 4,
        colors = listOf(
            Color(r = 15, g = 56, b = 15, percentage = 35.0),
            Color(r = 48, g = 98, b = 48, percentage = 30.0),
            Color(r = 139, g = 172, b = 15, percentage = 20.0),
            Color(r = 155, g = 188, b = 15, percentage = 15.0)
        )
    )

    val GAME_GEAR = ColorPalette(
        category = Category.HANDHELD,
        name = "Game Gear",
        colorCount = 12,
        colors = listOf(
            Color(r = 0, g = 0, b = 0, percentage = 10.0),
            Color(r = 85, g = 85, b = 85, percentage = 8.0),
            Color(r = 170, g = 170, b = 170, percentage = 8.0),
            Color(r = 255, g = 255, b = 255, percentage = 8.0),
            Color(r = 170, g = 0, b = 0, percentage = 8.5),
            Color(r = 0, g = 170, b = 0, percentage = 8.5),
            Color(r = 0, g = 0, b = 170, percentage = 8.5),
            Color(r = 170, g = 170, b = 0, percentage = 8.5),
            Color(r = 170, g = 0, b = 170, percentage = 8.5),
            Color(r = 0, g = 170, b = 170, percentage = 8.5),
            Color(r = 255, g = 85, b = 85, percentage = 8.0),
            Color(r = 85, g = 255, b = 85, percentage = 8.0)
        )
    )


    val BUBBLEGUM = ColorPalette(
        category = Category.MODERN,
        name = "Bubblegum",
        colorCount = 8,
        colors = listOf(
            Color(r = 8, g = 8, b = 8, percentage = 12.5),
            Color(r = 34, g = 34, b = 34, percentage = 12.5),
            Color(r = 66, g = 60, b = 65, percentage = 12.5),
            Color(r = 61, g = 92, b = 189, percentage = 12.5),
            Color(r = 196, g = 65, b = 134, percentage = 12.5),
            Color(r = 64, g = 178, b = 227, percentage = 12.5),
            Color(r = 129, g = 138, b = 218, percentage = 12.5),
            Color(r = 173, g = 192, b = 235, percentage = 12.5)
        )
    )

    val PASTEL_DREAMS = ColorPalette(
        category = Category.MODERN,
        name = "Pastel Dreams",
        colorCount = 8,
        colors = listOf(
            Color(r = 255, g = 247, b = 243, percentage = 12.5),
            Color(r = 250, g = 218, b = 221, percentage = 12.5),
            Color(r = 235, g = 209, b = 248, percentage = 12.5),
            Color(r = 207, g = 231, b = 245, percentage = 12.5),
            Color(r = 196, g = 223, b = 216, percentage = 12.5),
            Color(r = 255, g = 231, b = 171, percentage = 12.5),
            Color(r = 255, g = 206, b = 173, percentage = 12.5),
            Color(r = 222, g = 193, b = 222, percentage = 12.5)
        )
    )

    val TANGERINE = ColorPalette(
        category = Category.MODERN,
        name = "Tangerine",
        colorCount = 8,
        colors = listOf(
            Color(r = 2, g = 2, b = 2, percentage = 24.330357142857142),
            Color(r = 22, g = 22, b = 22, percentage = 16.964285714285715),
            Color(r = 77, g = 77, b = 79, percentage = 7.56917631917632),
            Color(r = 254, g = 131, b = 0, percentage = 15.813022844272846),
            Color(r = 217, g = 218, b = 215, percentage = 7.598334942084942),
            Color(r = 180, g = 254, b = 228, percentage = 11.840411840411841),
            Color(r = 210, g = 253, b = 238, percentage = 2.245213963963964),
            Color(r = 247, g = 247, b = 247, percentage = 13.639197232947234)
        )
    )


    val SUNSET_84 = ColorPalette(
        category = Category.SYNTHWAVE,
        name = "Sunset '84",
        colorCount = 6,
        colors = listOf(
            Color(r = 22, g = 9, b = 49, percentage = 20.0),
            Color(r = 70, g = 22, b = 94, percentage = 18.0),
            Color(r = 153, g = 34, b = 122, percentage = 17.0),
            Color(r = 248, g = 113, b = 113, percentage = 15.0),
            Color(r = 253, g = 186, b = 116, percentage = 15.0),
            Color(r = 255, g = 237, b = 171, percentage = 15.0)
        )
    )

    val CYBERPUNK = ColorPalette(
        category = Category.SYNTHWAVE,
        name = "Cyberpunk",
        colorCount = 6,
        colors = listOf(
            Color(r = 10, g = 10, b = 25, percentage = 25.0),
            Color(r = 33, g = 0, b = 79, percentage = 15.0),
            Color(r = 255, g = 0, b = 110, percentage = 20.0),
            Color(r = 0, g = 255, b = 197, percentage = 15.0),
            Color(r = 255, g = 213, b = 0, percentage = 15.0),
            Color(r = 0, g = 120, b = 255, percentage = 10.0)
        )
    )

    val MIDNIGHT_NEON = ColorPalette(
        category = Category.SYNTHWAVE,
        name = "Midnight Neon",
        colorCount = 6,
        colors = listOf(
            Color(r = 10, g = 14, b = 52, percentage = 18.0),
            Color(r = 27, g = 42, b = 120, percentage = 16.0),
            Color(r = 13, g = 192, b = 232, percentage = 18.0),
            Color(r = 255, g = 45, b = 138, percentage = 18.0),
            Color(r = 255, g = 214, b = 10, percentage = 15.0),
            Color(r = 76, g = 255, b = 173, percentage = 15.0)
        )
    )


    val PURPLE = ColorPalette(
        category = Category.RETRO,
        name = "Purple",
        colorCount = 8,
        colors = listOf(
            Color(r = 8, g = 8, b = 12, percentage = 18.0),
            Color(r = 27, g = 20, b = 53, percentage = 14.0),
            Color(r = 48, g = 32, b = 106, percentage = 12.0),
            Color(r = 73, g = 48, b = 158, percentage = 12.0),
            Color(r = 96, g = 72, b = 208, percentage = 12.0),
            Color(r = 123, g = 110, b = 242, percentage = 12.0),
            Color(r = 170, g = 150, b = 242, percentage = 10.0),
            Color(r = 244, g = 120, b = 200, percentage = 10.0)
        )
    )

    val ARCADE_POP = ColorPalette(
        category = Category.RETRO,
        name = "Arcade Pop",
        colorCount = 6,
        colors = listOf(
            Color(r = 4, g = 28, b = 52, percentage = 17.0),
            Color(r = 0, g = 168, b = 232, percentage = 17.0),
            Color(r = 255, g = 45, b = 85, percentage = 16.0),
            Color(r = 255, g = 120, b = 0, percentage = 17.0),
            Color(r = 255, g = 219, b = 88, percentage = 16.0),
            Color(r = 144, g = 0, b = 255, percentage = 17.0)
        )
    )


    val DESERT_GLOW = ColorPalette(
        category = Category.ATMOSPHERIC,
        name = "Desert Glow",
        colorCount = 6,
        colors = listOf(
            Color(r = 27, g = 14, b = 3, percentage = 20.0),
            Color(r = 85, g = 40, b = 11, percentage = 20.0),
            Color(r = 145, g = 76, b = 28, percentage = 20.0),
            Color(r = 214, g = 132, b = 45, percentage = 15.0),
            Color(r = 241, g = 173, b = 69, percentage = 15.0),
            Color(r = 254, g = 232, b = 182, percentage = 10.0)
        )
    )

    val AUTUMN_ORCHARD = ColorPalette(
        category = Category.ATMOSPHERIC,
        name = "Autumn Orchard",
        colorCount = 5,
        colors = listOf(
            Color(r = 39, g = 24, b = 4, percentage = 25.0),
            Color(r = 83, g = 46, b = 12, percentage = 20.0),
            Color(r = 133, g = 75, b = 29, percentage = 20.0),
            Color(r = 192, g = 120, b = 32, percentage = 20.0),
            Color(r = 226, g = 166, b = 79, percentage = 15.0)
        )
    )
    
    /**
     * Returns all predefined palettes organized by category and historical significance.
     */
    fun getAllPalettes(): List<ColorPalette> = listOf(

        COMMODORE_64,
        APPLE_II,
        ZX_SPECTRUM,
        TELETEXT,
        
        GAME_BOY_GREEN,
        GAME_GEAR,

        BUBBLEGUM,
        PASTEL_DREAMS,
        TANGERINE,

        SUNSET_84,
        CYBERPUNK,
        MIDNIGHT_NEON,
        
        PURPLE,
        ARCADE_POP,
        
        DESERT_GLOW,
        AUTUMN_ORCHARD
    )
}

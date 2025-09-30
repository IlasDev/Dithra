package dev.ilas.dithra.data.repository

import android.content.Context
import android.content.SharedPreferences
import dev.ilas.dithra.data.model.ColorPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit
import dev.ilas.dithra.data.model.Category

/**
 * Repository for managing persistent storage of custom color palettes.
 * Uses SharedPreferences with JSON serialization for reliability.
 */
class PaletteRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "custom_palettes", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_CUSTOM_PALETTES = "custom_palettes_list"
    }
    
    /**
     * Loads all custom palettes from persistent storage.
     *
     * @return List of custom color palettes
     */
    suspend fun loadCustomPalettes(): List<ColorPalette> = withContext(Dispatchers.IO) {
        try {
            val palettesJson = sharedPreferences.getString(KEY_CUSTOM_PALETTES, null)
            if (palettesJson.isNullOrEmpty()) {
                return@withContext emptyList()
            }
            
            val palettesArray = JSONArray(palettesJson)
            val palettes = mutableListOf<ColorPalette>()
            
            for (i in 0 until palettesArray.length()) {
                val paletteObj = palettesArray.getJSONObject(i)
                val palette = parsePaletteFromJson(paletteObj)
                if (palette != null) {
                    palettes.add(palette)
                }
            }
            
            palettes
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Saves all custom palettes to persistent storage.
     *
     * @param palettes List of custom color palettes to save
     */
    suspend fun saveCustomPalettes(palettes: List<ColorPalette>) = withContext(Dispatchers.IO) {
        try {
            val palettesArray = JSONArray()
            
            for (palette in palettes) {
                val paletteObj = paletteToJson(palette)
                palettesArray.put(paletteObj)
            }
            
            sharedPreferences.edit {
                putString(KEY_CUSTOM_PALETTES, palettesArray.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Adds or updates a custom palette.
     *
     * @param palette The palette to add or update
     */
    suspend fun upsertCustomPalette(palette: ColorPalette) {
        val currentPalettes = loadCustomPalettes().toMutableList()
        val existingIndex = currentPalettes.indexOfFirst { it.id == palette.id }
        
        if (existingIndex >= 0) {
            currentPalettes[existingIndex] = palette
        } else {
            currentPalettes.add(palette)
        }
        
        saveCustomPalettes(currentPalettes.sortedBy { it.name.lowercase() })
    }
    
    /**
     * Deletes a custom palette by ID.
     *
     * @param paletteId ID of the palette to delete
     */
    suspend fun deleteCustomPalette(paletteId: String) {
        val currentPalettes = loadCustomPalettes().toMutableList()
        currentPalettes.removeAll { it.id == paletteId }
        saveCustomPalettes(currentPalettes)
    }
    
    /**
     * Clears all custom palettes.
     */
    fun clearAllCustomPalettes() {
        sharedPreferences.edit {
            remove(KEY_CUSTOM_PALETTES)
        }
    }

    /**
     * Converts ColorPalette to JSON object.
     */
    private fun paletteToJson(palette: ColorPalette): JSONObject {
        val paletteObj = JSONObject()
        paletteObj.put("id", palette.id)
        paletteObj.put("name", palette.name)
        paletteObj.put("category", palette.category)
        paletteObj.put("colorCount", palette.colorCount)
        paletteObj.put("isCustom", palette.isCustom)
        
        val colorsArray = JSONArray()
        for (color in palette.colors) {
            val colorObj = JSONObject()
            colorObj.put("r", color.r)
            colorObj.put("g", color.g)
            colorObj.put("b", color.b)
            colorObj.put("percentage", color.percentage)
            colorsArray.put(colorObj)
        }
        paletteObj.put("colors", colorsArray)
        
        return paletteObj
    }
    
    /**
     * Parses ColorPalette from JSON object.
     */
    private fun parsePaletteFromJson(paletteObj: JSONObject): ColorPalette? {
        return try {
            val colors = mutableListOf<dev.ilas.dithra.data.model.Color>()
            val colorsArray = paletteObj.getJSONArray("colors")
            
            for (i in 0 until colorsArray.length()) {
                val colorObj = colorsArray.getJSONObject(i)
                val color = dev.ilas.dithra.data.model.Color(
                    r = colorObj.getInt("r"),
                    g = colorObj.getInt("g"),
                    b = colorObj.getInt("b"),
                    percentage = colorObj.getDouble("percentage")
                )
                colors.add(color)
            }
            
            ColorPalette(
                id = paletteObj.getString("id"),
                name = paletteObj.getString("name"),
                category = Category.CUSTOM,
                colors = colors,
                colorCount = paletteObj.getInt("colorCount"),
                isCustom = paletteObj.optBoolean("isCustom", true)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
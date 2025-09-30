package dev.ilas.dithra.presentation.ui.export

import android.content.Context
import android.graphics.Bitmap
import dev.ilas.dithra.R

/**
 * Export file format types.
 *
 * @param extension File extension
 */
enum class ExportType(val extension: String) {
    PNG("png"),
    SVG("svg");
    
    /**
     * Gets the display name for this export type.
     * 
     * @param context Context for accessing string resources
     * @return Localized display name
     */
    fun getDisplayName(context: Context): String {
        return when (this) {
            PNG -> context.getString(R.string.export_type_png)
            SVG -> context.getString(R.string.export_type_svg)
        }
    }
}

/**
 * Export resolution options for different output sizes.
 *
 * @param shortAxis Target size for shorter axis in pixels
 */
enum class ExportResolution(val shortAxis: Int) {
    HD_1920(1920),
    FHD_1080(1080),
    SMALL_512(512),
    ORIGINAL(-1),
    PIXEL_SIZE(-2);
    
    /**
     * Gets display name for this resolution option.
     *
     * @param context Context for accessing string resources
     * @param bitmap Optional bitmap for dimension calculation
     * @param pixelDimensions Optional true pixel dimensions
     * @return Human-readable display name
     */
    fun getDisplayName(context: Context, bitmap: Bitmap?, pixelDimensions: Pair<Int, Int>? = null): String {
        return when (this) {
            HD_1920 -> {
                val (width, height) = calculateDimensions(bitmap, 1920)
                "${width}×${height}"
            }
            FHD_1080 -> {
                val (width, height) = calculateDimensions(bitmap, 1080)
                "${width}×${height}"
            }
            SMALL_512 -> {
                val (width, height) = calculateDimensions(bitmap, 512)
                "${width}×${height}"
            }
            ORIGINAL -> context.getString(R.string.export_resolution_original)
            PIXEL_SIZE -> {
                val width = pixelDimensions?.first ?: bitmap?.width
                val height = pixelDimensions?.second ?: bitmap?.height
                if (width != null && height != null) {
                    context.getString(R.string.export_resolution_actual)
                } else {
                    context.getString(R.string.export_resolution_actual)
                }
            }
        }
    }
    
    /**
     * Calculates output dimensions for this resolution.
     *
     * @param bitmap Source bitmap for aspect ratio calculation
     * @param targetShortAxis Override for target short axis
     * @return Pair of (width, height) for output
     */
    fun calculateDimensions(bitmap: Bitmap?, targetShortAxis: Int? = null): Pair<Int, Int> {
        if (bitmap == null) return Pair(1920, 1920)
        
        return when (this) {
            ORIGINAL -> Pair(bitmap.width, bitmap.height)
            PIXEL_SIZE -> Pair(bitmap.width, bitmap.height)
            else -> {
                val axis = targetShortAxis ?: shortAxis
                val width = bitmap.width
                val height = bitmap.height
                
                if (width <= height) {
                    val ratio = height.toFloat() / width.toFloat()
                    Pair(axis, (axis * ratio).toInt())
                } else {
                    val ratio = width.toFloat() / height.toFloat()
                    Pair((axis * ratio).toInt(), axis)
                }
            }
        }
    }
}
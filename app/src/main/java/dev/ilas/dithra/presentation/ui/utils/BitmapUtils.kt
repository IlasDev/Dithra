package dev.ilas.dithra.presentation.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dev.ilas.dithra.data.model.TransparencyMode
import dev.ilas.dithra.presentation.ui.export.ExportResolution
import dev.ilas.dithra.presentation.ui.export.ExportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import dev.ilas.dithra.R

/**
 * Bitmap processing and manipulation utilities.
 * 
 * Provides comprehensive bitmap operations for:
 * - Export processing with progress tracking
 * - Scaling with different interpolation methods
 * - Transparency handling and mode application
 * - File system operations via Storage Access Framework
 */
object BitmapUtils {
    
    /**
     * Processes bitmap for export with progress tracking and transparency options.
     * 
     * @param bitmap Source bitmap to process
     * @param resolution Target resolution for export
     * @param transparentWhite Whether to make white pixels transparent
     * @param pixelDimensions True pixel dimensions for accurate sizing
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return Processed bitmap ready for export
     */
    suspend fun processForExport(
        bitmap: Bitmap,
        resolution: ExportResolution,
        transparentWhite: Boolean,
        pixelDimensions: Pair<Int, Int>?,
        onProgress: (Float) -> Unit
    ): Bitmap = withContext(Dispatchers.IO) {
        onProgress(0.1f)
        
        val transparentBitmap = if (transparentWhite) {
            onProgress(0.3f)
            makeWhiteTransparent(bitmap)
        } else {
            bitmap
        }
        
        onProgress(0.6f)
        
        if (resolution == ExportResolution.PIXEL_SIZE) {
            val targetWidth = pixelDimensions?.first?.takeIf { it > 0 } ?: transparentBitmap.width
            val targetHeight = pixelDimensions?.second?.takeIf { it > 0 } ?: transparentBitmap.height

            if (targetWidth != transparentBitmap.width || targetHeight != transparentBitmap.height) {
                onProgress(0.8f)
                val scaled = scaleNearestNeighbor(transparentBitmap, targetWidth, targetHeight)
                if (transparentBitmap != bitmap && transparentBitmap != scaled) {
                    transparentBitmap.recycle()
                }
                onProgress(1.0f)
                return@withContext scaled
            }

            onProgress(1.0f)
            return@withContext transparentBitmap
        }
        
        val (targetWidth, targetHeight) = resolution.calculateDimensions(transparentBitmap)
        
        val finalBitmap = if (targetWidth != transparentBitmap.width || targetHeight != transparentBitmap.height) {
            onProgress(0.8f)
            scaleNearestNeighbor(transparentBitmap, targetWidth, targetHeight)
        } else {
            transparentBitmap
        }
        
        if (transparentBitmap != bitmap && transparentBitmap != finalBitmap) {
            transparentBitmap.recycle()
        }
        
        onProgress(1.0f)
        finalBitmap
    }
    
    /**
     * Scales bitmap to fit within maximum dimensions while maintaining aspect ratio.
     * 
     * @param bitmap Source bitmap to scale
     * @param maxDimension Maximum size for the larger dimension
     * @return Scaled bitmap or original if no scaling needed
     */
    fun scaleToFitMaxDimension(bitmap: Bitmap, maxDimension: Int = 256): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        if (originalWidth <= maxDimension && originalHeight <= maxDimension) {
            return bitmap
        }
        
        val scaleFactor = if (originalWidth >= originalHeight) {
            maxDimension.toFloat() / originalWidth.toFloat()
        } else {
            maxDimension.toFloat() / originalHeight.toFloat()
        }
        
        val newWidth = (originalWidth.toFloat() * scaleFactor).toInt()
        val newHeight = (originalHeight.toFloat() * scaleFactor).toInt()
        
        val finalWidth: Int
        val finalHeight: Int
        
        if (originalWidth >= originalHeight) {
            finalWidth = maxDimension
            finalHeight = newHeight
        } else {
            finalWidth = newWidth  
            finalHeight = maxDimension
        }
        
        return bitmap.scale(finalWidth, finalHeight)
    }

    /**
     * Scales bitmap using nearest neighbor interpolation to preserve sharp pixels.
     * 
     * @param bitmap Source bitmap to scale
     * @param newWidth Target width in pixels
     * @param newHeight Target height in pixels
     * @return Scaled bitmap with preserved pixel sharpness
     */
    fun scaleNearestNeighbor(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        val scaledBitmap = createBitmap(newWidth, newHeight, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        val scaleX = originalWidth.toFloat() / newWidth
        val scaleY = originalHeight.toFloat() / newHeight
        
        val pixels = IntArray(newWidth * newHeight)
        
        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val sourceX = (x * scaleX).toInt().coerceIn(0, originalWidth - 1)
                val sourceY = (y * scaleY).toInt().coerceIn(0, originalHeight - 1)
                
                val pixel = bitmap[sourceX, sourceY]
                pixels[y * newWidth + x] = pixel
            }
        }
        
        scaledBitmap.setPixels(pixels, 0, newWidth, 0, 0, newWidth, newHeight)
        return scaledBitmap
    }
    
    /**
     * Makes white pixels transparent in bitmap.
     * 
     * @param bitmap Source bitmap to process
     * @return New bitmap with white pixels made transparent
     */
    fun makeWhiteTransparent(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = android.graphics.Color.red(pixel)
            val green = android.graphics.Color.green(pixel)
            val blue = android.graphics.Color.blue(pixel)
            
            if (red > 240 && green > 240 && blue > 240) {
                pixels[i] = android.graphics.Color.TRANSPARENT
            }
        }
        
        val transparentBitmap = createBitmap(width, height)
        transparentBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return transparentBitmap
    }
    
    /**
     * Applies transparency mode processing to bitmap with transparency.
     * 
     * @param bitmap Source bitmap to process (should support transparency)
     * @param transparencyMode How to handle transparent pixels
     * @return New bitmap with transparency mode applied
     */
    fun applyTransparencyMode(bitmap: Bitmap, transparencyMode: TransparencyMode): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val backgroundColor = when (transparencyMode) {
            TransparencyMode.BLACK -> android.graphics.Color.BLACK
            TransparencyMode.WHITE -> android.graphics.Color.WHITE
        }
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = android.graphics.Color.alpha(pixel)
            
            if (alpha < 255) {
                if (alpha == 0) {
                    pixels[i] = backgroundColor
                } else {
                    val red = android.graphics.Color.red(pixel)
                    val green = android.graphics.Color.green(pixel)
                    val blue = android.graphics.Color.blue(pixel)
                    
                    val bgRed = android.graphics.Color.red(backgroundColor)
                    val bgGreen = android.graphics.Color.green(backgroundColor)
                    val bgBlue = android.graphics.Color.blue(backgroundColor)
                    
                    val alphaF = alpha / 255f
                    val invAlpha = 1f - alphaF
                    
                    val finalRed = (red * alphaF + bgRed * invAlpha).toInt().coerceIn(0, 255)
                    val finalGreen = (green * alphaF + bgGreen * invAlpha).toInt().coerceIn(0, 255)
                    val finalBlue = (blue * alphaF + bgBlue * invAlpha).toInt().coerceIn(0, 255)
                    
                    pixels[i] = android.graphics.Color.rgb(finalRed, finalGreen, finalBlue)
                }
            }
        }
        
        val resultBitmap = createBitmap(width, height)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return resultBitmap
    }
    
    /**
     * Saves processed bitmap to file using Storage Access Framework.
     * 
     * @param context Android context
     * @param bitmap Bitmap to save
     * @param uri Target file URI
     * @param exportType Export format type
     */
    suspend fun saveBitmapToFile(
        context: Context,
        bitmap: Bitmap,
        uri: Uri,
        exportType: ExportType
    ) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                when (exportType) {
                    ExportType.PNG -> {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    ExportType.SVG -> {
                        throw IllegalStateException(context.getString(R.string.error_svg_export))
                    }
                }
                outputStream.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
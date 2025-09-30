package dev.ilas.dithra.core.processing

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import kotlin.math.abs
import androidx.core.graphics.createBitmap

/**
 * Utility functions for bitmap processing and manipulation.
 */
object BitmapUtils {
    /**
     * Applies pixel stretching to a bitmap.
     *
     * @param bitmap Source bitmap
     * @param factor Stretch factor (positive for vertical, negative for horizontal)
     * @return Stretched bitmap
     */
    fun applyPixelStretch(bitmap: Bitmap, factor: Float): Bitmap {
        if (factor == 0f) return bitmap

        val width = bitmap.width
        val height = bitmap.height

        val srcPixels = IntArray(width * height)
        bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val destPixels = IntArray(width * height) { Color.TRANSPARENT }
        val stretchRatio = 1.0f + (abs(factor) / 10.0f)

        if (factor > 0) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val sourceY = (y / stretchRatio).toInt()

                    if (sourceY < height) {
                        val srcIndex = sourceY * width + x
                        val destIndex = y * width + x
                        destPixels[destIndex] = srcPixels[srcIndex]
                    }
                }
            }
        } else {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val sourceX = (x / stretchRatio).toInt()

                    if (sourceX < width) {
                        val srcIndex = y * width + sourceX
                        val destIndex = y * width + x
                        destPixels[destIndex] = srcPixels[srcIndex]
                    }
                }
            }
        }

        return Bitmap.createBitmap(destPixels, width, height, Bitmap.Config.ARGB_8888)
    }

    /**
     * Adjusts brightness of a grayscale image.
     *
     * @param bitmap Source bitmap
     * @param threshold Brightness threshold (0-1)
     * @return Brightness-adjusted bitmap
     */
    fun adjustBrightness(bitmap: Bitmap, threshold: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val normalizedThreshold = threshold.coerceIn(0f, 1f)
        val adjustment = (normalizedThreshold - 0.5f) * 255f

        for (i in pixels.indices) {
            val originalAlpha = Color.alpha(pixels[i])
            val gray = Color.red(pixels[i])
            val newGray = (gray + adjustment).coerceIn(0f, 255f).toInt()
            pixels[i] = Color.argb(originalAlpha, newGray, newGray, newGray)
        }

        val resultBitmap = createBitmap(width, height)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return resultBitmap
    }

    /**
     * Applies threshold to convert image to black and white.
     *
     * @param bitmap Source bitmap
     * @param threshold Threshold value (0-1)
     * @return Thresholded bitmap
     */
    fun applyThreshold(bitmap: Bitmap, threshold: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val normalizedThreshold = threshold.coerceIn(0f, 1f)
        val thresholdValue = (255 * normalizedThreshold).toInt()

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            
            if (alpha < 50) {
                pixels[i] = Color.TRANSPARENT
            } else {
                val gray = Color.red(pixel)
                pixels[i] = if (gray > thresholdValue) Color.TRANSPARENT else Color.BLACK
            }
        }

        val resultBitmap = createBitmap(width, height)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return resultBitmap
    }

    /**
     * Stretches bitmap by a factor.
     *
     * @param bitmap Source bitmap
     * @param factor Stretch factor
     * @return Stretched bitmap
     */
    fun stretch(bitmap: Bitmap, factor: Float): Bitmap {
        if (factor == 1.0f) return bitmap
        val matrix = Matrix().apply { postScale(1f, factor) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    /**
     * Converts bitmap to grayscale while preserving transparency.
     *
     * @param bitmap Source bitmap
     * @return Grayscale bitmap
     */
    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            
            if (alpha < 50) {
                pixels[i] = Color.TRANSPARENT
            } else {
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                pixels[i] = Color.argb(alpha, gray, gray, gray)
            }
        }
        
        val grayscaleBitmap = createBitmap(width, height)
        grayscaleBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return grayscaleBitmap
    }
}
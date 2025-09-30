package dev.ilas.dithra.core.processing

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap

/**
 * Halftone pattern generation for bitmap processing.
 */
object Halftone {

    /**
     * Creates a Bayer 2x2 dithering pattern from a grayscale source image.
     *
     * @param bitmap Source grayscale bitmap
     * @param threshold Threshold adjustment factor (0-1)
     * @return Halftone-patterned bitmap
     */
    fun bayer2x2(bitmap: Bitmap, threshold: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val resultBitmap = createBitmap(width, height)
        resultBitmap.eraseColor(Color.WHITE)

        val sourcePixels = IntArray(width * height)
        bitmap.getPixels(sourcePixels, 0, width, 0, 0, width, height)

        val resultPixels = IntArray(width * height) { Color.WHITE }

        val bayerMatrix = arrayOf(
            intArrayOf(0, 2),
            intArrayOf(3, 1)
        )
        val normalizedThreshold = threshold.coerceIn(0f, 1f)
        val adjustedThreshold = (normalizedThreshold * 255).toInt()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = sourcePixels[y * width + x]
                val alpha = Color.alpha(pixel)
                
                if (alpha < 50) {
                    continue
                }
                
                val gray = Color.red(pixel)
                val matrixValue = bayerMatrix[y % 2][x % 2]
                val bayerThreshold = (matrixValue * 64) + adjustedThreshold - 128
                
                if (gray < bayerThreshold) {
                    resultPixels[y * width + x] = Color.BLACK
                }
            }
        }

        resultBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return resultBitmap
    }
}
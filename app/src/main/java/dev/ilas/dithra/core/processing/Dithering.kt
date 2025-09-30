package dev.ilas.dithra.core.processing

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap

/**
 * Dithering algorithms for bitmap processing.
 */
object Dithering {

    /**
     * Applies Floyd-Steinberg error diffusion dithering.
     *
     * @param bitmap Source bitmap
     * @param threshold Brightness threshold
     * @param factor Error diffusion strength (0-30)
     * @return Dithered bitmap
     */
    fun floydSteinberg(bitmap: Bitmap, threshold: Float, factor: Float = 15f): Bitmap {
        val adjustedBitmap = BitmapUtils.adjustBrightness(bitmap, threshold)
        return applyErrorDiffusion(adjustedBitmap,
            arrayOf(
                intArrayOf(0, 0, 7),
                intArrayOf(3, 5, 1)
            ), 16, factor)
    }

    /**
     * Applies Atkinson error diffusion dithering.
     *
     * @param bitmap Source bitmap
     * @param threshold Brightness threshold
     * @param factor Error diffusion strength (0-30)
     * @return Dithered bitmap
     */
    fun atkinson(bitmap: Bitmap, threshold: Float, factor: Float = 15f): Bitmap {
        val adjustedBitmap = BitmapUtils.adjustBrightness(bitmap, threshold)
        return applyErrorDiffusion(adjustedBitmap,
            arrayOf(
                intArrayOf(0, 0, 1, 1),
                intArrayOf(1, 1, 1, 0),
                intArrayOf(0, 1, 0, 0)
            ), 8, factor)
    }

    /**
     * Applies Jarvis-Judice-Ninke error diffusion dithering.
     *
     * @param bitmap Source bitmap
     * @param threshold Brightness threshold
     * @param factor Error diffusion strength (0-30)
     * @return Dithered bitmap
     */
    fun jarvisJudiceNinke(bitmap: Bitmap, threshold: Float, factor: Float = 15f): Bitmap {
        val adjustedBitmap = BitmapUtils.adjustBrightness(bitmap, threshold)
        return applyErrorDiffusion(adjustedBitmap,
            arrayOf(
                intArrayOf(0, 0, 0, 7, 5),
                intArrayOf(3, 5, 7, 5, 3),
                intArrayOf(1, 3, 5, 3, 1)
            ), 48, factor)
    }

    /**
     * Applies Stucki error diffusion dithering.
     *
     * @param bitmap Source bitmap
     * @param threshold Brightness threshold
     * @param factor Error diffusion strength (0-30)
     * @return Dithered bitmap
     */
    fun stucki(bitmap: Bitmap, threshold: Float, factor: Float = 15f): Bitmap {
        val adjustedBitmap = BitmapUtils.adjustBrightness(bitmap, threshold)
        return applyErrorDiffusion(adjustedBitmap,
            arrayOf(
                intArrayOf(0, 0, 0, 8, 4),
                intArrayOf(2, 4, 8, 4, 2),
                intArrayOf(1, 2, 4, 2, 1)
            ), 42, factor)
    }

    /**
     * Applies 4x4 Bayer ordered dithering.
     *
     * @param bitmap Source bitmap
     * @param thresholdFactor Threshold adjustment factor (0-1)
     * @return Dithered bitmap
     */
    fun bayer4x4(bitmap: Bitmap, thresholdFactor: Float): Bitmap {
        val matrix = arrayOf(
            intArrayOf(0, 8, 2, 10),
            intArrayOf(12, 4, 14, 6),
            intArrayOf(3, 11, 1, 9),
            intArrayOf(15, 7, 13, 5)
        )
        return applyOrderedDithering(bitmap, matrix, 16, thresholdFactor)
    }

    /**
     * Applies 8x8 Bayer ordered dithering.
     *
     * @param bitmap Source bitmap
     * @param thresholdFactor Threshold adjustment factor (0-1)
     * @return Dithered bitmap
     */
    fun bayer8x8(bitmap: Bitmap, thresholdFactor: Float): Bitmap {
        val matrix = arrayOf(
            intArrayOf(0, 32, 8, 40, 2, 34, 10, 42), intArrayOf(48, 16, 56, 24, 50, 18, 58, 26),
            intArrayOf(12, 44, 4, 36, 14, 46, 6, 38), intArrayOf(60, 28, 52, 20, 62, 30, 54, 22),
            intArrayOf(3, 35, 11, 43, 1, 33, 9, 41), intArrayOf(51, 19, 59, 27, 49, 17, 57, 25),
            intArrayOf(15, 47, 7, 39, 13, 45, 5, 37), intArrayOf(63, 31, 55, 23, 61, 29, 53, 21)
        )
        return applyOrderedDithering(bitmap, matrix, 64, thresholdFactor)
    }

    /**
     * Applies 4x4 clustered dot ordered dithering.
     *
     * @param bitmap Source bitmap
     * @param thresholdFactor Threshold adjustment factor (0-1)
     * @return Dithered bitmap
     */
    fun clustered4x4(bitmap: Bitmap, thresholdFactor: Float): Bitmap {
        val matrix = arrayOf(
            intArrayOf(12, 5, 6, 13),
            intArrayOf(4, 0, 1, 7),
            intArrayOf(11, 3, 2, 8),
            intArrayOf(15, 10, 9, 14)
        )
        return applyOrderedDithering(bitmap, matrix, 16, thresholdFactor)
    }

    /**
     * Applies error diffusion algorithm with specified matrix.
     */
    private fun applyErrorDiffusion(bitmap: Bitmap, matrix: Array<IntArray>, divisor: Int, factor: Float = 15f): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val grayPixels = pixels.map { 
            val alpha = Color.alpha(it)
            if (alpha < 50) -1f else Color.red(it).toFloat()
        }.toMutableList()
        val matrixWidth = matrix[0].size
        val matrixOffset = matrixWidth / 2

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val oldPixel = grayPixels[index]
                
                if (oldPixel < 0) {
                    pixels[index] = Color.TRANSPARENT
                    continue
                }
                
                val newPixel = if (oldPixel > 127.5f) 255f else 0f
                grayPixels[index] = newPixel
                pixels[index] = if (newPixel > 127.5f) Color.TRANSPARENT else Color.BLACK
                val error = oldPixel - newPixel

                for (row in matrix.indices) {
                    for (col in 0 until matrixWidth) {
                        val matrixFactor = matrix[row][col]
                        if (matrixFactor == 0) continue

                        val neighborX = x + col - matrixOffset
                        val neighborY = y + row
                        if (neighborX in 0 until width && neighborY in 0 until height) {
                            val neighborIndex = neighborY * width + neighborX
                            if (grayPixels[neighborIndex] >= 0) {
                                val factorScale = factor / 30f
                                val scaledError = error * factorScale
                                val newColor = grayPixels[neighborIndex] + (scaledError * matrixFactor / divisor)
                                grayPixels[neighborIndex] = newColor.coerceIn(0f, 255f)
                            }
                        }
                    }
                }
            }
        }
        val result = createBitmap(width, height)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Applies ordered dithering algorithm with specified matrix.
     */
    private fun applyOrderedDithering(bitmap: Bitmap, matrix: Array<IntArray>, divisor: Int, thresholdFactor: Float): Bitmap {
        val matrixSize = matrix.size
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val normalizedFactor = thresholdFactor.coerceIn(0f, 1f)
        val bias = (normalizedFactor - 0.5f) * 0.5f

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = Color.alpha(pixel)
                
                if (alpha < 50) {
                    pixels[index] = Color.TRANSPARENT
                } else {
                    val gray = Color.red(pixel) / 255f
                    val matrixValue = matrix[y % matrixSize][x % matrixSize]
                    val matrixThreshold = (matrixValue + 0.5f) / divisor
                    val adjustedGray = (gray + bias).coerceIn(0f, 1f)

                    pixels[index] = if (adjustedGray > matrixThreshold) Color.TRANSPARENT else Color.BLACK
                }
            }
        }
        val result = createBitmap(width, height)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
}
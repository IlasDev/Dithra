package dev.ilas.dithra.core.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.scale
import dev.ilas.dithra.data.model.ColorPalette
import dev.ilas.dithra.data.model.DitheringMethod
import dev.ilas.dithra.data.model.ImageProcessingOptions
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

/**
 * Result of image processing operation.
 *
 * @param bitmap Rendered display bitmap
 * @param pixelBitmap Pixel-accurate bitmap for export
 * @param pixelWidth Width in pixels
 * @param pixelHeight Height in pixels
 */
data class ProcessingResult(
    val bitmap: Bitmap,
    val pixelBitmap: Bitmap,
    val pixelWidth: Int,
    val pixelHeight: Int
)

/**
 * Internal render result container.
 */
private data class RenderResult(
    val displayBitmap: Bitmap,
    val pixelBitmap: Bitmap
)

/**
 * Processes images with various dithering and halftone algorithms.
 */
class ImageProcessor {

    private val RENDER_SIZE = 1024
    private val MAX_PROCESSING_SIZE = 400

    /**
     * Processes an image with specified options.
     *
     * @param options Processing configuration
     * @return Processing result with display and pixel bitmaps
     */
    fun process(options: ImageProcessingOptions): ProcessingResult {
        var baseBitmap = options.originalBitmap

        if (baseBitmap.width > MAX_PROCESSING_SIZE || baseBitmap.height > MAX_PROCESSING_SIZE) {
            val newWidth: Int
            val newHeight: Int
            val aspectRatio = baseBitmap.height.toFloat() / baseBitmap.width.toFloat()

            if (baseBitmap.width > baseBitmap.height) {
                newWidth = MAX_PROCESSING_SIZE
                newHeight = (newWidth * aspectRatio).toInt().coerceAtLeast(1)
            } else {
                newHeight = MAX_PROCESSING_SIZE
                newWidth = (newHeight / aspectRatio).toInt().coerceAtLeast(1)
            }
            baseBitmap = baseBitmap.scale(newWidth, newHeight, true)
        }
        val grayscaleBitmap = BitmapUtils.toGrayscale(baseBitmap)

        val aspectRatio = grayscaleBitmap.height.toFloat() / grayscaleBitmap.width.toFloat()
        val longestDimension = options.size.coerceIn(1, MAX_PROCESSING_SIZE)

        val (targetWidth, targetHeight) = if (grayscaleBitmap.height >= grayscaleBitmap.width) {
            val targetHeight = longestDimension
            val targetWidth = (targetHeight / aspectRatio).roundToInt().coerceAtLeast(1)
            targetWidth to targetHeight
        } else {
            val targetWidth = longestDimension
            val targetHeight = (targetWidth * aspectRatio).roundToInt().coerceAtLeast(1)
            targetWidth to targetHeight
        }

        val smallSourceBitmap = grayscaleBitmap.scale(targetWidth, targetHeight, filter = true)

        val smallProcessedBitmap = when (options.ditheringMethod) {
            DitheringMethod.BAYER_2X2 -> Halftone.bayer2x2(smallSourceBitmap, options.threshold)
            DitheringMethod.BITMAP -> BitmapUtils.applyThreshold(smallSourceBitmap, options.threshold)
            DitheringMethod.FLOYD_STEINBERG -> Dithering.floydSteinberg(smallSourceBitmap, options.threshold, options.factor)
            DitheringMethod.ATKINSON -> Dithering.atkinson(smallSourceBitmap, options.threshold, options.factor)
            DitheringMethod.JARVIS_JUDICE_NINKE -> Dithering.jarvisJudiceNinke(smallSourceBitmap, options.threshold, options.factor)
            DitheringMethod.STUCKI -> Dithering.stucki(smallSourceBitmap, options.threshold, options.factor)
            DitheringMethod.BAYER_4X4 -> Dithering.bayer4x4(smallSourceBitmap, options.threshold)
            DitheringMethod.BAYER_8X8 -> Dithering.bayer8x8(smallSourceBitmap, options.threshold)
            DitheringMethod.CLUSTERED_4X4 -> Dithering.clustered4x4(smallSourceBitmap, options.threshold)
        }

        val renderResult = renderSquarePixelBitmap(
            mask = smallProcessedBitmap,
            grayscaleSource = smallSourceBitmap,
            palette = options.selectedPalette
        )

        return ProcessingResult(
            bitmap = renderResult.displayBitmap,
            pixelBitmap = renderResult.pixelBitmap,
            pixelWidth = smallProcessedBitmap.width,
            pixelHeight = smallProcessedBitmap.height
        )
    }

    /**
     * Renders the final image using square pixels with optional color palette.
     *
     * @param mask Black and white mask bitmap
     * @param grayscaleSource Original grayscale source
     * @param palette Optional color palette for rendering
     * @return Render result with display and pixel bitmaps
     */
    private fun renderSquarePixelBitmap(
        mask: Bitmap,
        grayscaleSource: Bitmap,
        palette: ColorPalette?
    ): RenderResult {
        if (palette == null) {
            return renderBlackAndWhite(mask)
        }

        val aspectRatio = mask.height.toFloat() / mask.width.toFloat()
        val largeHeight = (RENDER_SIZE * aspectRatio).toInt().coerceAtLeast(1)
        val largeBitmap = createBitmap(RENDER_SIZE, largeHeight)
        val canvas = Canvas(largeBitmap)
        val pixelBitmap = createBitmap(mask.width.coerceAtLeast(1), mask.height.coerceAtLeast(1))

        val sortedColors = palette.colors.sortedBy { color ->
            0.299 * color.r + 0.587 * color.g + 0.114 * color.b
        }

        val backgroundColor = sortedColors.lastOrNull()?.toColorInt()
            ?: palette.colors.firstOrNull()?.toColorInt()
            ?: Color.WHITE
        canvas.drawColor(backgroundColor)
        pixelBitmap.eraseColor(backgroundColor)

        val paint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        val maskWidth = mask.width.coerceAtLeast(1)
        val maskHeight = mask.height.coerceAtLeast(1)
        val pixelWidth = RENDER_SIZE.toFloat() / maskWidth.toFloat()
        val pixelHeight = largeHeight.toFloat() / maskHeight.toFloat()

        val maskPixels = IntArray(maskWidth * maskHeight)
        mask.getPixels(maskPixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)

        val grayscaleWidth = grayscaleSource.width.coerceAtLeast(1)
        val grayscaleHeight = grayscaleSource.height.coerceAtLeast(1)
        val grayscalePixels = IntArray(grayscaleWidth * grayscaleHeight)
        grayscaleSource.getPixels(
            grayscalePixels,
            0,
            grayscaleWidth,
            0,
            0,
            grayscaleWidth,
            grayscaleHeight
        )

        val bayerMatrix = arrayOf(
            intArrayOf(0, 8, 2, 10),
            intArrayOf(12, 4, 14, 6),
            intArrayOf(3, 11, 1, 9),
            intArrayOf(15, 7, 13, 5)
        )
        val matrixSize = bayerMatrix.size
        val matrixMax = (matrixSize * matrixSize - 1).coerceAtLeast(1)

        var currentY = 0f
        for (y in 0 until maskHeight) {
            var currentX = 0f
            for (x in 0 until maskWidth) {
                val maskIndex = y * maskWidth + x

                if (maskPixels[maskIndex] == Color.BLACK) {
                    val pixelColor = if (sortedColors.size == 1) {
                        sortedColors[0].toColorInt()
                    } else {
                        val sourceX = (((x + 0.5f) * grayscaleWidth) / maskWidth)
                            .toInt()
                            .coerceIn(0, grayscaleWidth - 1)
                        val sourceY = (((y + 0.5f) * grayscaleHeight) / maskHeight)
                            .toInt()
                            .coerceIn(0, grayscaleHeight - 1)
                        val grayscaleIndex = sourceY * grayscaleWidth + sourceX
                        val gray = Color.red(grayscalePixels[grayscaleIndex])
                        val intensity = gray / 255f
                        val verticalFactor = if (maskHeight > 1) y / (maskHeight - 1f) else 0f
                        val biasedIntensity = (intensity - (verticalFactor - 0.5f) * 0.25f)
                            .coerceIn(0f, 1f)

                        val paletteRange = (sortedColors.size - 1).toFloat()
                        val palettePosition = biasedIntensity * paletteRange

                        val lowerIndex = floor(palettePosition).toInt().coerceIn(0, sortedColors.lastIndex)
                        val upperIndex = ceil(palettePosition).toInt().coerceIn(0, sortedColors.lastIndex)
                        val fraction = palettePosition - lowerIndex

                        val threshold = bayerMatrix[y % matrixSize][x % matrixSize] / matrixMax.toFloat()
                        val chosenIndex = if (fraction > threshold) upperIndex else lowerIndex

                        sortedColors[chosenIndex].toColorInt()
                    }
                    paint.color = pixelColor
                    canvas.drawRect(currentX, currentY, currentX + pixelWidth, currentY + pixelHeight, paint)
                    pixelBitmap[x, y] = pixelColor
                }
                
                currentX += pixelWidth
            }
            currentY += pixelHeight
        }
        return RenderResult(largeBitmap, pixelBitmap)
    }
    
    /**
     * Renders black and white image when no palette is selected.
     *
     * @param mask Black and white mask bitmap
     * @return Render result with black and white bitmaps
     */
    private fun renderBlackAndWhite(mask: Bitmap): RenderResult {
        val aspectRatio = mask.height.toFloat() / mask.width.toFloat()
        val largeHeight = (RENDER_SIZE * aspectRatio).toInt().coerceAtLeast(1)
        val largeBitmap = createBitmap(RENDER_SIZE, largeHeight)
        val canvas = Canvas(largeBitmap)
        canvas.drawColor(Color.WHITE)
        val pixelBitmap = createBitmap(mask.width.coerceAtLeast(1), mask.height.coerceAtLeast(1))
        pixelBitmap.eraseColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        val pixelWidth = RENDER_SIZE.toFloat() / mask.width.toFloat()
        val pixelHeight = largeHeight.toFloat() / mask.height.toFloat()

        val pixels = IntArray(mask.width * mask.height)
        mask.getPixels(pixels, 0, mask.width, 0, 0, mask.width, mask.height)

        var currentY = 0f
        for (y in 0 until mask.height) {
            var currentX = 0f
            for (x in 0 until mask.width) {
                if (pixels[y * mask.width + x] == Color.BLACK) {
                    canvas.drawRect(currentX, currentY, currentX + pixelWidth, currentY + pixelHeight, paint)
                    pixelBitmap[x, y] = Color.BLACK
                }
                currentX += pixelWidth
            }
            currentY += pixelHeight
        }
        return RenderResult(largeBitmap, pixelBitmap)
    }
}
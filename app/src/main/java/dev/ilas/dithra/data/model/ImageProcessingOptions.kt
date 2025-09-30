package dev.ilas.dithra.data.model

import android.graphics.Bitmap
import dev.ilas.dithra.R

/**
 * Available dithering and processing methods.
 *
 * @param labelResId String resource ID for display name
 */
enum class DitheringMethod(val labelResId: Int) {
    BITMAP(R.string.dithering_method_bitmap),
    FLOYD_STEINBERG(R.string.dithering_method_floyd_steinberg),
    ATKINSON(R.string.dithering_method_atkinson),
    JARVIS_JUDICE_NINKE(R.string.dithering_method_jarvis_judice_ninke),
    STUCKI(R.string.dithering_method_stucki),
    BAYER_2X2(R.string.dithering_method_bayer_2x2),
    BAYER_4X4(R.string.dithering_method_bayer_4x4),
    BAYER_8X8(R.string.dithering_method_bayer_8x8),
    CLUSTERED_4X4(R.string.dithering_method_clustered_4x4)
}

/**
 * Checks if dithering method supports error diffusion factor parameter.
 */
fun DitheringMethod.supportsFactor(): Boolean {
    return when (this) {
        DitheringMethod.FLOYD_STEINBERG,
        DitheringMethod.ATKINSON,
        DitheringMethod.JARVIS_JUDICE_NINKE,
        DitheringMethod.STUCKI -> true
        else -> false
    }
}

/**
 * Configuration options for image processing.
 *
 * @param originalBitmap Source bitmap to process
 * @param size Target size for longest dimension
 * @param threshold Brightness threshold (0.0f to 1.0f)
 * @param ditheringMethod Selected processing method
 * @param factor Error diffusion strength (0f to 30f)
 * @param selectedPalette Color palette (null for black & white)
 */
data class ImageProcessingOptions(
    val originalBitmap: Bitmap,
    val size: Int = 128,
    val threshold: Float = 0.5f,
    val ditheringMethod: DitheringMethod = DitheringMethod.BITMAP,
    val factor: Float = 15f,
    val selectedPalette: ColorPalette? = null
)
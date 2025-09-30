package dev.ilas.dithra.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ilas.dithra.core.processing.ImageProcessor
import dev.ilas.dithra.data.model.ColorPalette
import dev.ilas.dithra.data.model.DitheringMethod
import dev.ilas.dithra.data.model.ImageProcessingOptions
import dev.ilas.dithra.data.model.ThemeMode
import dev.ilas.dithra.data.model.TransparencyMode
import dev.ilas.dithra.data.repository.ExportRepository
import dev.ilas.dithra.data.repository.PaletteRepository
import dev.ilas.dithra.data.repository.SettingsRepository
import dev.ilas.dithra.presentation.ui.export.ExportResolution
import dev.ilas.dithra.presentation.ui.export.ExportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap
import dev.ilas.dithra.R
import dev.ilas.dithra.data.model.Category

/**
 * Represents the current UI state for image processing parameters.
 *
 * @property originalBitmap Source bitmap for processing
 * @property sizeSliderValue Size adjustment value (0-50 range)
 * @property thresholdSliderValue Dithering threshold value (0-100 range)
 * @property ditheringMethod Selected dithering algorithm
 * @property factorSliderValue Factor value for applicable dithering methods
 * @property selectedPalette Color palette for processing, null for black & white
 */
data class UiState(
    val originalBitmap: Bitmap,
    val sizeSliderValue: Float = 25f,
    val thresholdSliderValue: Float = 27f,
    val ditheringMethod: DitheringMethod = DitheringMethod.BITMAP,
    val factorSliderValue: Float = 15f,
    val selectedPalette: ColorPalette? = null
)

/**
 * Main ViewModel coordinating image processing pipeline and application state.
 * 
 * Responsibilities:
 * - Image processing coordination and result management
 * - UI state persistence and restoration
 * - Custom palette lifecycle management
 * - Settings and export configuration
 * - Cross-component state synchronization
 *
 * @param context Application context for repository access
 * @param savedStateHandle State preservation across configuration changes
 */
class MainViewModel(
    private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val imageProcessor = ImageProcessor()
    private val paletteRepository = PaletteRepository(context)
    private val settingsRepository = SettingsRepository(context)
    private val exportRepository = ExportRepository(context)
    private var processingJob: Job? = null

    companion object {
        private const val KEY_SIZE_SLIDER = "size_slider_value"
        private const val KEY_THRESHOLD_SLIDER = "threshold_slider_value"
        private const val KEY_DITHERING_METHOD = "dithering_method"
        private const val KEY_FACTOR_SLIDER = "factor_slider_value"
        private const val KEY_SELECTED_PALETTE_ID = "selected_palette_id"
    }

    private val _uiState = MutableStateFlow(
        UiState(
            originalBitmap = createPlaceholderBitmap(),
            sizeSliderValue = savedStateHandle.get<Float>(KEY_SIZE_SLIDER) ?: 25f,
            thresholdSliderValue = savedStateHandle.get<Float>(KEY_THRESHOLD_SLIDER) ?: 27f,
            ditheringMethod = savedStateHandle.get<String>(KEY_DITHERING_METHOD)?.let { 
                DitheringMethod.entries.find { method -> method.name == it }
            } ?: DitheringMethod.BITMAP,
            factorSliderValue = savedStateHandle.get<Float>(KEY_FACTOR_SLIDER) ?: 15f,
            selectedPalette = null
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _processedBitmap = MutableStateFlow<Bitmap?>(null)
    val processedBitmap = _processedBitmap.asStateFlow()

    private val _pixelDimensions = MutableStateFlow<Pair<Int, Int>?>(null)
    val pixelDimensions = _pixelDimensions.asStateFlow()

    private val _pixelBitmap = MutableStateFlow<Bitmap?>(null)
    val pixelBitmap = _pixelBitmap.asStateFlow()

    private val _customPalettes = MutableStateFlow<List<ColorPalette>>(emptyList())
    val customPalettes = _customPalettes.asStateFlow()
    
    val appSettings = settingsRepository.settings
    val exportSettings = exportRepository.exportSettings
    
    private val _showSettingsScreen = MutableStateFlow(false)
    val showSettingsScreen = _showSettingsScreen.asStateFlow()
    
    init {
        loadCustomPalettes()
        processImage()
    }

    /**
     * Sets the source bitmap for processing.
     */
    fun setOriginalBitmap(bitmap: Bitmap) {
        _uiState.update { it.copy(originalBitmap = bitmap) }
        processImage()
    }
    
    /**
     * Sets source bitmap with transparency mode preprocessing.
     * Applies current transparency settings before processing.
     */
    fun setOriginalBitmapWithTransparencyProcessing(bitmap: Bitmap) {
        val transparencyProcessedBitmap = dev.ilas.dithra.presentation.ui.utils.BitmapUtils.applyTransparencyMode(
            bitmap, 
            appSettings.value.transparencyMode
        )
        setOriginalBitmap(transparencyProcessedBitmap)
        
        if (transparencyProcessedBitmap != bitmap) {
            bitmap.recycle()
        }
    }

    /**
     * Updates size parameter and triggers reprocessing.
     */
    fun updateSize(sliderValue: Float) {
        _uiState.update { it.copy(sizeSliderValue = sliderValue) }
        savedStateHandle[KEY_SIZE_SLIDER] = sliderValue
        processImage()
    }

    /**
     * Updates threshold parameter and triggers reprocessing.
     */
    fun updateThreshold(sliderValue: Float) {
        _uiState.update { it.copy(thresholdSliderValue = sliderValue) }
        savedStateHandle[KEY_THRESHOLD_SLIDER] = sliderValue
        processImage()
    }

    /**
     * Updates dithering method and triggers reprocessing.
     */
    fun updateDitheringMethod(method: DitheringMethod) {
        _uiState.update { it.copy(ditheringMethod = method) }
        savedStateHandle[KEY_DITHERING_METHOD] = method.name
        processImage()
    }

    /**
     * Updates factor parameter and triggers reprocessing.
     */
    fun updateFactor(sliderValue: Float) {
        _uiState.update { it.copy(factorSliderValue = sliderValue) }
        savedStateHandle[KEY_FACTOR_SLIDER] = sliderValue
        processImage()
    }

    /**
     * Updates selected palette and triggers reprocessing.
     */
    fun updatePalette(palette: ColorPalette?) {
        _uiState.update { it.copy(selectedPalette = palette) }
        savedStateHandle[KEY_SELECTED_PALETTE_ID] = palette?.id
        processImage()
    }

    /**
     * Creates or updates custom palette with validation and persistence.
     * Sanitizes color data and maintains sorted collection.
     */
    fun upsertCustomPalette(palette: ColorPalette) {
        val sanitizedColors = palette.colors
            .take(ColorPalette.MAX_COLORS)
            .map { it.copy(percentage = 0.0) }
        if (sanitizedColors.isEmpty()) {
            return
        }
        val evenPercentage = 100.0 / sanitizedColors.size
        val normalizedColors = sanitizedColors.map { color ->
            color.copy(percentage = evenPercentage.coerceAtMost(100.0))
        }
        val sanitizedPalette = palette.copy(
            category = Category.CUSTOM,
            colors = normalizedColors,
            colorCount = normalizedColors.size,
            isCustom = true,
            id = palette.id.ifBlank { java.util.UUID.randomUUID().toString() }
        )

        _customPalettes.update { existing ->
            val updated = (existing.filterNot { it.id == sanitizedPalette.id } + sanitizedPalette)
                .sortedBy { it.name.lowercase() }
            
            viewModelScope.launch {
                paletteRepository.saveCustomPalettes(updated)
            }
            
            updated
        }

        if (_uiState.value.selectedPalette?.id == sanitizedPalette.id) {
            _uiState.update { it.copy(selectedPalette = sanitizedPalette) }
            processImage()
        }
    }

    /**
     * Removes custom palette and handles active selection updates.
     */
    fun deleteCustomPalette(paletteId: String) {
        var removed = false
        _customPalettes.update { existing ->
            val filtered = existing.filterNot { it.id == paletteId }
            removed = filtered.size != existing.size
            
            if (removed) {
                viewModelScope.launch {
                    paletteRepository.saveCustomPalettes(filtered)
                }
            }
            
            filtered
        }

        if (removed && _uiState.value.selectedPalette?.id == paletteId) {
            _uiState.update { it.copy(selectedPalette = null) }
            savedStateHandle[KEY_SELECTED_PALETTE_ID] = null
            processImage()
        }
    }

    /**
     * Parses exported palette string format.
     */
    fun parsePaletteFromString(context: Context, raw: String): ColorPalette? {
        return ColorPalette.fromExportString(context, raw)
    }

    /**
     * Coordinates image processing pipeline with current parameters.
     * Cancels previous operations and processes on background thread.
     */
    private fun processImage() {
        processingJob?.cancel()
        val currentState = _uiState.value

        _pixelDimensions.value = null
        processingJob = viewModelScope.launch {
            val effectiveSize = mapSize(
                sliderValue = currentState.sizeSliderValue,
                originalWidth = currentState.originalBitmap.width,
                originalHeight = currentState.originalBitmap.height
            )
            val effectiveThreshold = (currentState.thresholdSliderValue / 75f).coerceIn(0f, 1f)

            val options = ImageProcessingOptions(
                originalBitmap = currentState.originalBitmap,
                size = effectiveSize,
                threshold = effectiveThreshold,
                ditheringMethod = currentState.ditheringMethod,
                factor = currentState.factorSliderValue,
                selectedPalette = currentState.selectedPalette
            )

            val processingResult = withContext(Dispatchers.Default) {
                imageProcessor.process(options)
            }

            _pixelDimensions.value = processingResult.pixelWidth to processingResult.pixelHeight
            _processedBitmap.value = processingResult.bitmap
            _pixelBitmap.value = processingResult.pixelBitmap
        }
    }

    /**
     * Maps slider value to actual pixel dimensions.
     * Uses longest dimension as baseline, capped at 400px.
     */
    private fun mapSize(sliderValue: Float, originalWidth: Int, originalHeight: Int): Int {
        val minSize = 8
        var maxSize = originalWidth.coerceAtLeast(originalHeight).coerceAtLeast(minSize)
        if (maxSize > 400) {
            maxSize = 400
        }
        val ratio = sliderValue / 50f
        return (minSize + (maxSize - minSize) * ratio).roundToInt()
    }

    /**
     * Loads persisted custom palettes and restores active selection.
     */
    private fun loadCustomPalettes() {
        viewModelScope.launch {
            val palettes = paletteRepository.loadCustomPalettes()
            _customPalettes.value = palettes
            
            val savedPaletteId = savedStateHandle.get<String>(KEY_SELECTED_PALETTE_ID)
            if (savedPaletteId != null) {
                val savedPalette = palettes.find { it.id == savedPaletteId }
                if (savedPalette != null) {
                    _uiState.update { it.copy(selectedPalette = savedPalette) }
                }
            }
        }
    }

    /**
     * Creates placeholder bitmap for initial application state.
     */
    private fun createPlaceholderBitmap(): Bitmap {
        val size = 400
        val bitmap = createBitmap(size, size)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawPaint(paint)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 30f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText(context.getString(R.string.placeholder_pick_image), size / 2f, size / 2f, paint)
        return bitmap
    }
    
    /**
     * Shows the settings screen.
     */
    fun showSettingsScreen() {
        _showSettingsScreen.value = true
    }
    
    /**
     * Hides the settings screen.
     */
    fun hideSettingsScreen() {
        _showSettingsScreen.value = false
    }
    
    /**
     * Updates theme mode setting.
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        settingsRepository.updateThemeMode(themeMode)
    }
    
    /**
     * Updates dynamic colors setting.
     */
    fun updateDynamicColors(enabled: Boolean) {
        settingsRepository.updateDynamicColors(enabled)
    }
    
    /**
     * Updates transparency mode setting.
     */
    fun updateTransparencyMode(transparencyMode: TransparencyMode) {
        settingsRepository.updateTransparencyMode(transparencyMode)
    }
    
    /**
     * Updates export settings and persists user preferences.
     */
    fun updateExportSettings(exportType: ExportType, resolution: ExportResolution, transparentWhite: Boolean) {
        if (exportType == ExportType.PNG) {
            exportRepository.updateAllSettings(exportType, resolution, transparentWhite)
        } else {
            exportRepository.updateExportType(exportType)
            exportRepository.updateTransparentWhite(transparentWhite)
        }
    }
}
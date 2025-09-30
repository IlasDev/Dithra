@file:OptIn(ExperimentalMaterial3Api::class)

package dev.ilas.dithra.presentation.ui.screen

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ilas.dithra.R
import dev.ilas.dithra.data.model.ColorPalette
import dev.ilas.dithra.presentation.ui.components.ImageDisplaySection
import dev.ilas.dithra.presentation.ui.components.controls.BottomControlsSection
import dev.ilas.dithra.presentation.ui.components.controls.MiddleControlsSection
import dev.ilas.dithra.presentation.ui.components.controls.SizeSliderSection
import dev.ilas.dithra.presentation.ui.components.controls.ThresholdSliderSection
import dev.ilas.dithra.presentation.ui.dialogs.DitheringMethodBottomSheet
import dev.ilas.dithra.presentation.ui.dialogs.PaletteActionDialog
import dev.ilas.dithra.presentation.ui.dialogs.PaletteBottomSheet
import dev.ilas.dithra.presentation.ui.dialogs.color.CustomPaletteDialog
import dev.ilas.dithra.presentation.ui.export.ExportDialog
import dev.ilas.dithra.presentation.ui.export.ExportResolution
import dev.ilas.dithra.presentation.ui.export.ExportType
import dev.ilas.dithra.presentation.ui.utils.BitmapUtils
import dev.ilas.dithra.presentation.ui.utils.SVGUtils
import dev.ilas.dithra.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Main screen for bitmap processing with Material3 Expressive design
 * Completely modularized and organized for open-source development
 *
 * Features:
 * - Modular UI components in separate files
 * - Clean separation of concerns
 * - Reusable dialog components
 * - Utility functions for processing
 * - Modern Android architecture patterns
 *
 * @param viewModel The main view model handling image processing logic
 * @param modifier Optional modifier for the composable
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Material3ExpressiveScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val processedBitmap by viewModel.processedBitmap.collectAsStateWithLifecycle()
    val pixelBitmap by viewModel.pixelBitmap.collectAsStateWithLifecycle()
    val pixelDimensions by viewModel.pixelDimensions.collectAsStateWithLifecycle()
    val customPalettes by viewModel.customPalettes.collectAsStateWithLifecycle()
    val exportSettings by viewModel.exportSettings.collectAsStateWithLifecycle()

    var showDitheringBottomSheet by remember { mutableStateOf(false) }
    var showPaletteBottomSheet by remember { mutableStateOf(false) }
    var showCustomPaletteDialog by remember { mutableStateOf(false) }
    var showPaletteActionsDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    var paletteBeingEdited by remember { mutableStateOf<ColorPalette?>(null) }
    var paletteActionTarget by remember { mutableStateOf<ColorPalette?>(null) }
    var reopenPaletteSheetAfterDialog by remember { mutableStateOf(false) }

    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var exportedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var exportedSVGData by remember { mutableStateOf<String?>(null) }
    var pendingExport by remember { mutableStateOf<Triple<ExportType, ExportResolution, Boolean>?>(null) }

    var clipboardStatusMessage by remember { mutableStateOf<String?>(null) }

    clipboardStatusMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            clipboardStatusMessage = null
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                val mimeType = context.contentResolver.getType(it) ?: "unknown"
                if (mimeType == "image/svg+xml" || mimeType == "image/gif" ||
                    MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) in listOf("svg", "gif")
                ) {
                    Toast.makeText(context, R.string.error_unsupported_image_format, Toast.LENGTH_SHORT).show()
                    return@let
                }
                val originalBitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }.copy(Bitmap.Config.ARGB_8888, true)

                val scaledBitmap = BitmapUtils.scaleToFitMaxDimension(originalBitmap, 400)
                
                viewModel.setOriginalBitmapWithTransparencyProcessing(scaledBitmap)
                
                if (scaledBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }
            }
        }
    )

    val fileCreationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri: Uri? ->
            uri?.let { fileUri ->
                pendingExport?.let { (exportType, _, _) ->
                    coroutineScope.launch {
                        when (exportType) {
                            ExportType.PNG -> {
                                exportedBitmap?.let { bitmap ->
                                    BitmapUtils.saveBitmapToFile(
                                        context = context,
                                        bitmap = bitmap,
                                        uri = fileUri,
                                        exportType = exportType
                                    )
                                }
                            }
                            ExportType.SVG -> {
                                exportedSVGData?.let { svgData ->
                                    SVGUtils.saveSVGToFile(
                                        context = context,
                                        svgData = svgData,
                                        uri = fileUri
                                    )
                                }
                            }
                        }
                        exportedBitmap = null
                        exportedSVGData = null
                        isExporting = false
                        exportProgress = 0f
                    }
                }
                pendingExport = null
            } ?: run {
                exportedBitmap = null
                exportedSVGData = null
                isExporting = false
                exportProgress = 0f
                pendingExport = null
            }
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val isLandscape = maxWidth > maxHeight
            val heightDp = maxHeight
            
            val useLandscapeLayout = isLandscape && heightDp < 550.dp
            
            if (useLandscapeLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ImageDisplaySection(
                        processedBitmap = processedBitmap,
                        isExporting = isExporting,
                        exportProgress = exportProgress,
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .padding(16.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        SizeSliderSection(
                            value = uiState.sizeSliderValue,
                            onValueChange = { viewModel.updateSize(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ThresholdSliderSection(
                            value = uiState.thresholdSliderValue,
                            onValueChange = { viewModel.updateThreshold(it) },
                            selectedMethod = uiState.ditheringMethod
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        MiddleControlsSection(
                            value = uiState.thresholdSliderValue,
                            onValueChange = { viewModel.updateThreshold(it) },
                            factorValue = uiState.factorSliderValue,
                            onFactorValueChange = { viewModel.updateFactor(it) },
                            selectedMethod = uiState.ditheringMethod,
                            onMethodClick = { showDitheringBottomSheet = true },
                            onUploadClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BottomControlsSection(
                            onPaletteClick = { showPaletteBottomSheet = true },
                            onSaveClick = { showExportDialog = true }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    ImageDisplaySection(
                        processedBitmap = processedBitmap,
                        isExporting = isExporting,
                        exportProgress = exportProgress,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SizeSliderSection(
                        value = uiState.sizeSliderValue,
                        onValueChange = { viewModel.updateSize(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ThresholdSliderSection(
                        value = uiState.thresholdSliderValue,
                        onValueChange = { viewModel.updateThreshold(it) },
                        selectedMethod = uiState.ditheringMethod
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MiddleControlsSection(
                        value = uiState.thresholdSliderValue,
                        onValueChange = { viewModel.updateThreshold(it) },
                        factorValue = uiState.factorSliderValue,
                        onFactorValueChange = { viewModel.updateFactor(it) },
                        selectedMethod = uiState.ditheringMethod,
                        onMethodClick = { showDitheringBottomSheet = true },
                        onUploadClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BottomControlsSection(
                        onPaletteClick = { showPaletteBottomSheet = true },
                        onSaveClick = { showExportDialog = true }
                    )
                }
            }
        }

        DialogsSection(
            showDitheringBottomSheet = showDitheringBottomSheet,
            onDitheringDismiss = { showDitheringBottomSheet = false },
            selectedMethod = uiState.ditheringMethod,
            onMethodSelected = { method ->
                viewModel.updateDitheringMethod(method)
                showDitheringBottomSheet = false
            },
            showPaletteBottomSheet = showPaletteBottomSheet,
            onPaletteDismiss = { showPaletteBottomSheet = false },
            selectedPalette = uiState.selectedPalette,
            customPalettes = customPalettes,
            onPaletteSelected = { palette ->
                viewModel.updatePalette(palette)
                showPaletteBottomSheet = false
            },
            onCreateCustomPalette = {
                paletteBeingEdited = null
                reopenPaletteSheetAfterDialog = true
                showPaletteBottomSheet = false
                showCustomPaletteDialog = true
            },
            onCustomPaletteLongPress = { palette ->
                paletteActionTarget = palette
                showPaletteActionsDialog = true
            },
            showCustomPaletteDialog = showCustomPaletteDialog,
            onCustomPaletteDismiss = {
                showCustomPaletteDialog = false
                paletteBeingEdited = null
                if (reopenPaletteSheetAfterDialog) {
                    reopenPaletteSheetAfterDialog = false
                    showPaletteBottomSheet = true
                }
            },
            paletteBeingEdited = paletteBeingEdited,
            onCustomPaletteSave = { palette ->
                viewModel.upsertCustomPalette(palette)
                showCustomPaletteDialog = false
                paletteBeingEdited = null
                if (reopenPaletteSheetAfterDialog) {
                    reopenPaletteSheetAfterDialog = false
                    showPaletteBottomSheet = true
                }
            },
            onRequestImportData = { clipboardManager.getText()?.text },
            parseExportedPalette = { raw -> viewModel.parsePaletteFromString(context, raw) },
            showPaletteActionsDialog = showPaletteActionsDialog,
            paletteActionTarget = paletteActionTarget,
            onPaletteActionDismiss = {
                showPaletteActionsDialog = false
                paletteActionTarget = null
            },
            onPaletteEdit = {
                val target = paletteActionTarget
                if (target != null) {
                    showPaletteActionsDialog = false
                    paletteActionTarget = null
                    paletteBeingEdited = target
                    reopenPaletteSheetAfterDialog = true
                    showPaletteBottomSheet = false
                    showCustomPaletteDialog = true
                }
            },
            onPaletteDelete = {
                paletteActionTarget?.let { target ->
                    viewModel.deleteCustomPalette(target.id)
                }
                showPaletteActionsDialog = false
                paletteActionTarget = null
                showPaletteBottomSheet = true
                reopenPaletteSheetAfterDialog = false
            },
            onPaletteExport = {
                paletteActionTarget?.let { target ->
                    clipboardManager.setText(AnnotatedString(target.toExportString()))
                    clipboardStatusMessage = context.getString(R.string.palette_copied_clipboard)
                }
                showPaletteActionsDialog = false
                paletteActionTarget = null
            },
            onShowToast = { message -> clipboardStatusMessage = message },
            showExportDialog = showExportDialog,
            onExportDismiss = { showExportDialog = false },
            processedBitmap = processedBitmap,
            pixelDimensions = pixelDimensions,
            exportSettings = exportSettings,
            onExport = { exportType, resolution, transparentWhite ->
                viewModel.updateExportSettings(exportType, resolution, transparentWhite)
                
                showExportDialog = false
                isExporting = true
                exportProgress = 0f

                coroutineScope.launch {
                    processedBitmap?.let { bitmap ->
                        val baseBitmap: Bitmap = pixelBitmap ?: bitmap
                        val effectiveDimensions = pixelDimensions ?: (baseBitmap.width to baseBitmap.height)

                        when (exportType) {
                            ExportType.PNG -> {
                                val processedExportBitmap = BitmapUtils.processForExport(
                                    bitmap = baseBitmap,
                                    resolution = if (resolution == ExportResolution.PIXEL_SIZE) ExportResolution.PIXEL_SIZE else resolution,
                                    transparentWhite = transparentWhite,
                                    pixelDimensions = effectiveDimensions,
                                    onProgress = { progress -> exportProgress = progress }
                                )
                                exportedBitmap = processedExportBitmap
                            }
                            ExportType.SVG -> {
                                val svgData = SVGUtils.generateOptimizedSVG(
                                    bitmap = baseBitmap,
                                    resolution = if (resolution == ExportResolution.PIXEL_SIZE) ExportResolution.PIXEL_SIZE else resolution,
                                    transparentWhite = transparentWhite,
                                    pixelDimensions = effectiveDimensions,
                                    onProgress = { progress -> exportProgress = progress }
                                )
                                exportedSVGData = svgData
                            }
                        }
                        pendingExport = Triple(exportType, resolution, transparentWhite)

                        val timestamp = java.text.SimpleDateFormat(
                            context.getString(R.string.date_format_export),
                            java.util.Locale.getDefault()
                        ).format(java.util.Date())
                        val fileName = "${context.getString(R.string.filename_prefix_dithered)}${timestamp}.${exportType.extension}"

                        fileCreationLauncher.launch(fileName)
                    }
                }
            }
        )
    }
}

/**
 * Composable that organizes all dialog and bottom sheet components
 * Keeps the main screen composable clean and organized
 */
@Composable
private fun DialogsSection(
    showDitheringBottomSheet: Boolean,
    onDitheringDismiss: () -> Unit,
    selectedMethod: dev.ilas.dithra.data.model.DitheringMethod,
    onMethodSelected: (dev.ilas.dithra.data.model.DitheringMethod) -> Unit,
    showPaletteBottomSheet: Boolean,
    onPaletteDismiss: () -> Unit,
    selectedPalette: ColorPalette?,
    customPalettes: List<ColorPalette>,
    onPaletteSelected: (ColorPalette?) -> Unit,
    onCreateCustomPalette: () -> Unit,
    onCustomPaletteLongPress: (ColorPalette) -> Unit,
    showCustomPaletteDialog: Boolean,
    onCustomPaletteDismiss: () -> Unit,
    paletteBeingEdited: ColorPalette?,
    onCustomPaletteSave: (ColorPalette) -> Unit,
    onRequestImportData: () -> String?,
    parseExportedPalette: (String) -> ColorPalette?,
    showPaletteActionsDialog: Boolean,
    paletteActionTarget: ColorPalette?,
    onPaletteActionDismiss: () -> Unit,
    onPaletteEdit: () -> Unit,
    onPaletteDelete: () -> Unit,
    onPaletteExport: () -> Unit,
    showExportDialog: Boolean,
    onExportDismiss: () -> Unit,
    processedBitmap: Bitmap?,
    pixelDimensions: Pair<Int, Int>?,
    exportSettings: dev.ilas.dithra.data.model.ExportSettings,
    onExport: (ExportType, ExportResolution, Boolean) -> Unit,
    onShowToast: (String) -> Unit
) {
    if (showDitheringBottomSheet) {
        DitheringMethodBottomSheet(
            selectedMethod = selectedMethod,
            onMethodSelected = onMethodSelected,
            onDismiss = onDitheringDismiss
        )
    }

    if (showPaletteBottomSheet) {
        PaletteBottomSheet(
            selectedPalette = selectedPalette,
            customPalettes = customPalettes,
            onPaletteSelected = onPaletteSelected,
            onDismiss = onPaletteDismiss,
            onCreateCustomPalette = onCreateCustomPalette,
            onCustomPaletteLongPress = onCustomPaletteLongPress
        )
    }

    if (showCustomPaletteDialog) {
        CustomPaletteDialog(
            initialPalette = paletteBeingEdited,
            onDismiss = onCustomPaletteDismiss,
            onSave = onCustomPaletteSave,
            onRequestImportData = onRequestImportData,
            parseExportedPalette = parseExportedPalette,
            onShowToast = onShowToast
        )
    }

    if (showPaletteActionsDialog && paletteActionTarget != null) {
        PaletteActionDialog(
            palette = paletteActionTarget,
            onDismiss = onPaletteActionDismiss,
            onEdit = onPaletteEdit,
            onDelete = onPaletteDelete,
            onExport = onPaletteExport
        )
    }

    if (showExportDialog) {
        ExportDialog(
            processedBitmap = processedBitmap,
            pixelDimensions = pixelDimensions,
            initialSettings = exportSettings,
            onDismiss = onExportDismiss,
            onExport = onExport
        )
    }
}
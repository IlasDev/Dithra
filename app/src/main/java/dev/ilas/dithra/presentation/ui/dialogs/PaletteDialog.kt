@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)

package dev.ilas.dithra.presentation.ui.dialogs

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ilas.dithra.data.model.ColorPalette
import dev.ilas.dithra.R
import androidx.compose.ui.platform.LocalContext
import dev.ilas.dithra.data.model.Palette
import dev.ilas.dithra.presentation.ui.theme.extensions.getModularCornerShape
import kotlin.math.max
import kotlin.math.min

/**
 * Modal bottom sheet for selecting color palettes
 * Shows Black & White as default plus all available palettes
 *
 * @param modifier Optional modifier for styling
 * @param selectedPalette Currently selected color palette
 * @param customPalettes List of user-created custom palettes
 * @param onPaletteSelected Callback when a palette is selected
 * @param onDismiss Callback when dialog is dismissed
 * @param onCreateCustomPalette Callback when create new palette is clicked
 * @param onCustomPaletteLongPress Callback when a custom palette is long-pressed
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PaletteBottomSheet(
    selectedPalette: ColorPalette?,
    customPalettes: List<ColorPalette>,
    onPaletteSelected: (ColorPalette?) -> Unit,
    onDismiss: () -> Unit,
    onCreateCustomPalette: () -> Unit,
    onCustomPaletteLongPress: (ColorPalette) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val builtInPalettes = remember { Palette.getAllPalettes() }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalContext.current.getString(R.string.close))
            }
        },
        dismissButton = {
            TextButton(onClick = onCreateCustomPalette) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = LocalContext.current.getString(R.string.content_desc_add_palette),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(LocalContext.current.getString(R.string.palette_new))
            }
        },
        title = {
            Text(
                text = LocalContext.current.getString(R.string.palette_choose),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.55f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    PaletteRow(
                        palette = null,
                        isSelected = selectedPalette == null,
                        isCustom = false,
                        onClick = {
                            onPaletteSelected(null)
                            onDismiss()
                        },
                        index = 0,
                        totalItems = 1
                    )
                }

                if (customPalettes.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(2.dp)) }
                    item {
                        Text(
                            text = LocalContext.current.getString(R.string.palette_your),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                    itemsIndexed(customPalettes) { index, palette ->
                        PaletteRow(
                            palette = palette,
                            isSelected = palette.id == selectedPalette?.id,
                            isCustom = true,
                            onClick = {
                                onPaletteSelected(palette)
                                onDismiss()
                            },
                            onLongPress = { onCustomPaletteLongPress(palette) },
                            index = index,
                            totalItems = customPalettes.size
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(2.dp)) }
                item {
                    Text(
                        text = LocalContext.current.getString(R.string.palette_builtin),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                itemsIndexed(builtInPalettes) { index, palette ->
                    PaletteRow(
                        palette = palette,
                        isSelected = palette == selectedPalette,
                        isCustom = false,
                        onClick = {
                            onPaletteSelected(palette)
                            onDismiss()
                        },
                        index = index,
                        totalItems = builtInPalettes.size
                    )
                }
            }
        }
    )
}

/**
 * Individual row item for palette selection
 * Highlights selected palette with primary background color and check icon
 * Uses modular corner styling consistent with dithering method rows
 * 
 * @param palette The color palette to display (null for Black & White)
 * @param isSelected Whether this palette is currently selected
 * @param isCustom Whether this is a user-created custom palette
 * @param onClick Callback when the row is clicked
 * @param onLongPress Optional callback when the row is long-pressed
 * @param index Position of this item in the list
 * @param totalItems Total number of items in this section
 * @param modifier Optional modifier for styling
 */
@Composable
internal fun PaletteRow(
    modifier: Modifier = Modifier,
    palette: ColorPalette?,
    isSelected: Boolean,
    isCustom: Boolean,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    index: Int,
    totalItems: Int
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(getModularCornerShape(index, totalItems))
            .let { mod ->
                if (onLongPress != null) {
                    mod.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongPress
                    )
                } else {
                    mod.clickable(onClick = onClick)
                }
            },
        color = containerColor,
        shape = getModularCornerShape(index, totalItems)
    ) {
        SubcomposeLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) { constraints ->
            val looseConstraints = constraints.copy(minWidth = 0)

            val textPlaceable = subcompose("text") {
                Column {
                    Text(
                        text = palette?.name ?: LocalContext.current.getString(R.string.palette_black_and_white),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 16.sp,
                        color = contentColor,
                        fontWeight = FontWeight.Medium,
                    )

                    if (palette != null) {
                        Text(
                            text = buildString {
                                append("${palette.colorCount} colors")
                                append(" • ")
                                append(LocalContext.current.getString(palette.category.nameResId))
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )

                        /* if (isCustom) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                }
                            ) {
                                Text(
                                    text = LocalContext.current.getString(R.string.palette_your_palette),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = contentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } */
                    }
                }
            }.first().measure(looseConstraints)

            val availableWidthForTrailing = max(constraints.maxWidth - textPlaceable.width, 0)

            val trailingPlaceable = subcompose("trailing") {
                PaletteRowTrailingContent(
                    palette = palette,
                    contentColor = contentColor,
                    isSelected = isSelected,
                    availableWidthPx = availableWidthForTrailing
                )
            }.first().measure(looseConstraints.copy(maxWidth = availableWidthForTrailing))

            val layoutWidth = if (constraints.hasBoundedWidth) {
                constraints.maxWidth
            } else {
                textPlaceable.width + trailingPlaceable.width
            }
            val layoutHeight = max(textPlaceable.height, trailingPlaceable.height)

            val textY = (layoutHeight - textPlaceable.height) / 2
            val trailingY = (layoutHeight - trailingPlaceable.height) / 2

            layout(layoutWidth, layoutHeight) {
                textPlaceable.placeRelative(0, textY)
                trailingPlaceable.placeRelative(layoutWidth - trailingPlaceable.width, trailingY)
            }
        }
    }
}

@Composable
private fun PaletteRowTrailingContent(
    palette: ColorPalette?,
    contentColor: Color,
    isSelected: Boolean,
    availableWidthPx: Int
) {
    val density = LocalDensity.current
    val circleSizePx = with(density) { 12.dp.roundToPx() }
    val spacingPx = with(density) { 2.dp.roundToPx() }
    val plusIconWidthPx = with(density) { 16.dp.roundToPx() }
    val spacerWidthPx = with(density) { 8.dp.roundToPx() }
    val checkIconWidthPx = with(density) { 20.dp.roundToPx() }

    var remainingWidth = availableWidthPx
    val showCheckIcon = isSelected
    if (showCheckIcon) {
        remainingWidth -= checkIconWidthPx
    }

    var maxSwatches = 0
    var showOverflowIndicator = false

    if (palette != null && palette.colors.isNotEmpty() && remainingWidth > 0) {
        if (showCheckIcon) {
            remainingWidth -= spacerWidthPx
        }

        val desired = min(3, palette.colors.size)
        var candidate = desired
        while (candidate > 0) {
            val hasOverflow = palette.colors.size > candidate
            val items = candidate + if (hasOverflow) 1 else 0
            val rowWidth = candidate * circleSizePx + max(items - 1, 0) * spacingPx + if (hasOverflow) plusIconWidthPx else 0
            if (rowWidth <= remainingWidth) {
                maxSwatches = candidate
                showOverflowIndicator = hasOverflow
                break
            }
            candidate--
        }

        if (maxSwatches == 0 && remainingWidth >= circleSizePx) {
            maxSwatches = 1
            val canShowOverflow = palette.colors.size > maxSwatches
            val items = maxSwatches + if (canShowOverflow) 1 else 0
            val rowWidth = maxSwatches * circleSizePx + max(items - 1, 0) * spacingPx + if (canShowOverflow) plusIconWidthPx else 0
            showOverflowIndicator = canShowOverflow && rowWidth <= remainingWidth
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (palette != null && maxSwatches > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                palette.colors.takeLast(maxSwatches).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(color.toColorInt()),
                                CircleShape
                            )
                    )
                }
                if (showOverflowIndicator) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = LocalContext.current.getString(R.string.content_desc_more_colors),
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (showCheckIcon) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        if (showCheckIcon) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = LocalContext.current.getString(R.string.content_desc_selected),
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
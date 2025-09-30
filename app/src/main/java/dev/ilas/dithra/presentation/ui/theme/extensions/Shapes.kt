package dev.ilas.dithra.presentation.ui.theme.extensions

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Helper function to create modular corner shapes for grouped UI elements
 * Provides consistent corner styling across dithering methods and palette rows
 *
 * @param index The position of the item in the group (0-based)
 * @param totalItems Total number of items in the group
 * @return RoundedCornerShape with appropriate corner radii
 */
@Composable
fun getModularCornerShape(index: Int, totalItems: Int): RoundedCornerShape {
    val BIG_CORNER = 18.dp
    val SMALL_CORNER = 4.dp
    
    return when {
        totalItems == 1 -> RoundedCornerShape(BIG_CORNER)
        index == 0 -> RoundedCornerShape(BIG_CORNER, BIG_CORNER, SMALL_CORNER, SMALL_CORNER)
        index == totalItems - 1 -> RoundedCornerShape(SMALL_CORNER, SMALL_CORNER, BIG_CORNER, BIG_CORNER)
        else -> RoundedCornerShape(SMALL_CORNER)
    }
}
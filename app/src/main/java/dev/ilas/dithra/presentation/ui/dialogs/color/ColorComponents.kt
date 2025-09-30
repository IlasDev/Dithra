package dev.ilas.dithra.presentation.ui.dialogs.color

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.ilas.dithra.R
import androidx.compose.ui.platform.LocalContext

/**
 * Color swatch component for palette editing
 * Shows a color circle that can be clicked to edit or long-pressed to remove
 * 
 * @param color The color to display
 * @param onClick Callback when swatch is clicked
 * @param onRemove Callback when swatch is long-pressed
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorSwatch(
    color: Color,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onRemove
            )
    )
}

/**
 * Button for adding new colors to a palette
 * 
 * @param enabled Whether the button is enabled
 * @param onClick Callback when button is clicked
 * @param modifier Optional modifier for styling
 */
@Composable
fun ColorAddButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            /* .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                CircleShape
            ) */
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = LocalContext.current.getString(R.string.content_desc_add_color),
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}
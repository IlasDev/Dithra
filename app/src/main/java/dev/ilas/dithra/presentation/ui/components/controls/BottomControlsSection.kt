package dev.ilas.dithra.presentation.ui.components.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Bottom action buttons for palette selection and save operations.
 *
 * @param modifier Optional modifier for styling
 * @param onPaletteClick Callback when palette button is clicked
 * @param onSaveClick Callback when save button is clicked
 */
@Composable
fun BottomControlsSection(
    modifier: Modifier = Modifier,
    onPaletteClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPaletteClick,
            modifier = Modifier
                .size(80.dp, 60.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = "Palette",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onSaveClick,
            modifier = Modifier
                .size(80.dp, 60.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = "Save",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
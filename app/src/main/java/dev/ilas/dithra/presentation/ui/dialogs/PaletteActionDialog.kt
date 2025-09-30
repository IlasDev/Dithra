package dev.ilas.dithra.presentation.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ilas.dithra.data.model.ColorPalette
import dev.ilas.dithra.presentation.ui.theme.extensions.getModularCornerShape
import dev.ilas.dithra.R
import androidx.compose.ui.platform.LocalContext

/**
 * Dialog for managing custom palette actions (edit, export, delete)
 *
 * @param modifier Optional modifier for styling
 * @param palette The palette to manage
 * @param onDismiss Callback when dialog is dismissed
 * @param onEdit Callback when edit is selected
 * @param onDelete Callback when delete is selected
 * @param onExport Callback when export is selected
 */
@Composable
fun PaletteActionDialog(
    palette: ColorPalette,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(text = palette.name, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = LocalContext.current.getString(R.string.palette_manage), 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                PaletteActionRow(
                    text = LocalContext.current.getString(R.string.edit),
                    icon = Icons.Outlined.Palette,
                    onClick = onEdit,
                    index = 0,
                    totalItems = 3,
                    isDestructive = false
                )
                
                PaletteActionRow(
                    text = LocalContext.current.getString(R.string.palette_export_clipboard),
                    icon = Icons.Outlined.Save,
                    onClick = onExport,
                    index = 1,
                    totalItems = 3,
                    isDestructive = false
                )
                
                PaletteActionRow(
                    text = LocalContext.current.getString(R.string.delete),
                    icon = Icons.Default.Close,
                    onClick = onDelete,
                    index = 2,
                    totalItems = 3,
                    isDestructive = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalContext.current.getString(R.string.close))
            }
        }
    )
}

/**
 * Individual row item for palette actions
 * Follows the same design pattern as dithering method and palette selection rows
 * 
 * @param text The action text to display
 * @param icon The icon for the action
 * @param onClick Callback when the row is clicked
 * @param index Position of this item in the list
 * @param totalItems Total number of items in the list
 * @param isDestructive Whether this is a destructive action (uses error colors)
 * @param modifier Optional modifier for styling
 */
@Composable
private fun PaletteActionRow(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    index: Int,
    totalItems: Int,
    isDestructive: Boolean = false
) {
    val containerColor = if (isDestructive) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(getModularCornerShape(index, totalItems))
            .clickable { onClick() },
        color = containerColor,
        shape = getModularCornerShape(index, totalItems)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
        }
    }
}
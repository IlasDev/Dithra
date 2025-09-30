package dev.ilas.dithra.presentation.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ilas.dithra.presentation.ui.theme.extensions.getModularCornerShape

/**
 * Scrollable column container for dialog content with consistent spacing.
 *
 * @param modifier Optional modifier for the column
 * @param content Content to display in the scrollable column
 */
@Composable
fun DialogScrollColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

/**
 * Category section with icon and title for grouping related dialog options.
 *
 * @param title Category title text
 * @param icon Icon to display next to the title
 * @param modifier Optional modifier for the category container
 * @param content Category content composables
 */
@Composable
fun DialogCategory(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )
        }
        content()
    }
}

/**
 * Dropdown selection row for dialog forms with Material Design styling.
 *
 * @param label Primary label text
 * @param selectedValue Currently selected option
 * @param options List of available options
 * @param onSelectionChanged Callback when selection changes
 * @param index Position in the group (for corner radius styling)
 * @param totalItems Total items in the group (for corner radius styling)
 * @param modifier Optional modifier
 * @param supportingText Optional descriptive text below the label
 * @param enabled Whether the dropdown is interactive
 */
@Composable
fun DialogDropdownRow(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelectionChanged: (String) -> Unit,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    isSettings: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var rowWidthPx by remember { mutableIntStateOf(0) }
    var dropdownWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val horizontalOffset = remember(rowWidthPx, dropdownWidthPx, density) {
        with(density) { 
            val rowWidth = rowWidthPx.toDp()
            val dropdownWidth = dropdownWidthPx.toDp()
            rowWidth - dropdownWidth + 40.dp
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isSettings) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = getModularCornerShape(index, totalItems)
    ) {
        Column {
            val rowModifier = Modifier
                .fillMaxWidth()
                .then(
                    if (enabled) {
                        Modifier.clickable { expanded = true }
                    } else {
                        Modifier
                    }
                )
                .padding(20.dp)
                .onGloballyPositioned { coordinates ->
                    rowWidthPx = coordinates.size.width
                }

            Row(
                modifier = rowModifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    if (supportingText != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = supportingText,
                            lineHeight = 16.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.7f else 0.4f),
                    fontWeight = FontWeight.Medium
                )
            }

            if (enabled) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(horizontalOffset, 0.dp),
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        dropdownWidthPx = coordinates.size.width
                    }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onSelectionChanged(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Toggle switch row for dialog forms with Material Design styling.
 *
 * @param label Primary label text
 * @param checked Current toggle state
 * @param onCheckedChange Callback when toggle state changes
 * @param index Position in the group (for corner radius styling)
 * @param totalItems Total items in the group (for corner radius styling)
 * @param modifier Optional modifier
 * @param description Optional descriptive text below the label
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
    description: String? = null,
    isSettings: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isSettings) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = getModularCornerShape(index, totalItems)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        lineHeight = 16.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                )
            }
        }
    }
}

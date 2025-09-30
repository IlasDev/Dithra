@file:OptIn(ExperimentalMaterial3Api::class)

package dev.ilas.dithra.presentation.ui.dialogs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ilas.dithra.R
import androidx.compose.ui.platform.LocalContext
import dev.ilas.dithra.data.model.DitheringMethod
import dev.ilas.dithra.presentation.ui.theme.extensions.getModularCornerShape

/**
 * Alert dialog for selecting dithering methods
 * Shows all available methods with clear selection indication
 * 
 * @param selectedMethod Currently selected dithering method
 * @param onMethodSelected Callback when a method is selected
 * @param onDismiss Callback when dialog is dismissed
 * @param modifier Optional modifier for styling
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun DitheringMethodBottomSheet(
    selectedMethod: DitheringMethod,
    onMethodSelected: (DitheringMethod) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val allMethods = DitheringMethod.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalContext.current.getString(R.string.close), fontWeight = FontWeight.Medium)
            }
        },
        title = {
            Text(
                text = LocalContext.current.getString(R.string.dithering_choose_method),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.5f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(allMethods) { index, method ->
                    DitheringMethodRow(
                        index = index,
                        totalItems = allMethods.size,
                        method = method,
                        isSelected = method == selectedMethod,
                        onClick = {
                            onMethodSelected(method)
                            onDismiss()
                        }
                    )
                }
            }
        }
    )
}

/**
 * Individual row item for dithering method selection
 * Highlights selected method with primary background color and check icon
 * 
 * @param method The dithering method to display
 * @param isSelected Whether this method is currently selected
 * @param onClick Callback when the row is clicked
 * @param index Position of this item in the list
 * @param totalItems Total number of items in the list
 * @param modifier Optional modifier for styling
 */
@Composable
private fun DitheringMethodRow(
    method: DitheringMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        shape = getModularCornerShape(index, totalItems)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalContext.current.getString(method.labelResId),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 20.sp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.Medium
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = LocalContext.current.getString(R.string.content_desc_selected),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
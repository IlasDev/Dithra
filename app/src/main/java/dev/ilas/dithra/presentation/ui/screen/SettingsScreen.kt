package dev.ilas.dithra.presentation.ui.screen

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import dev.ilas.dithra.BuildConfig
import dev.ilas.dithra.data.model.AppSettings
import dev.ilas.dithra.data.model.ThemeMode
import dev.ilas.dithra.data.model.TransparencyMode
import dev.ilas.dithra.presentation.ui.dialogs.DialogCategory
import dev.ilas.dithra.presentation.ui.dialogs.DialogDropdownRow
import dev.ilas.dithra.presentation.ui.dialogs.DialogScrollColumn
import dev.ilas.dithra.presentation.ui.dialogs.DialogToggleRow
import dev.ilas.dithra.presentation.ui.theme.extensions.getModularCornerShape
import dev.ilas.dithra.R

/**
 * Settings screen with full screen layout.
 * Uses the same layout components as the original dialog for consistency.
 * Back navigation is handled by MainActivity.
 *
 * @param modifier Optional modifier for styling
 * @param settings Current application settings
 * @param onThemeModeChanged Called when theme selection changes
 * @param onDynamicColorsChanged Called when dynamic colors toggle changes
 * @param onTransparencyModeChanged Called when transparency mode changes
 * @param scrollBehavior Scroll behavior for the toolbar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settings: AppSettings,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onDynamicColorsChanged: (Boolean) -> Unit,
    onTransparencyModeChanged: (TransparencyMode) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .padding(16.dp, 0.dp, 16.dp, 16.dp)
    ) {
            DialogScrollColumn {
                DialogCategory(
                    title = context.getString(R.string.settings_appearance_title),
                    icon = Icons.Outlined.Palette
                ) {
                    DialogDropdownRow(
                        label = context.getString(R.string.settings_theme_label),
                        selectedValue = settings.themeMode.getDisplayName(context),
                        options = ThemeMode.entries.map { it.getDisplayName(context) },
                        onSelectionChanged = { displayName: String ->
                            ThemeMode.entries
                                .find { it.getDisplayName(context) == displayName }
                                ?.let(onThemeModeChanged)
                        },
                        index = 0,
                        totalItems = 2,
                        isSettings = true
                    )

                    DialogToggleRow(
                        label = context.getString(R.string.settings_dynamic_colors_label),
                        description = context.getString(R.string.settings_dynamic_colors_description),
                        checked = settings.dynamicColors,
                        onCheckedChange = onDynamicColorsChanged,
                        index = 1,
                        totalItems = 2,
                        isSettings = true
                    )
                }

                DialogCategory(
                    title = context.getString(R.string.settings_processing_title),
                    icon = Icons.Outlined.Image
                ) {
                    DialogDropdownRow(
                        label = context.getString(R.string.settings_png_background_label),
                        selectedValue = settings.transparencyMode.getDisplayName(context),
                        options = TransparencyMode.entries.map { it.getDisplayName(context) },
                        onSelectionChanged = { displayName: String ->
                            TransparencyMode.entries
                                .find { it.getDisplayName(context) == displayName }
                                ?.let(onTransparencyModeChanged)
                        },
                        index = 0,
                        totalItems = 1,
                        isSettings = true
                    )
                }

                val context = context
                DialogCategory(
                    title = context.getString(R.string.settings_information_title),
                    icon = Icons.Outlined.Info
                ) {
                    SettingsInfoRow(
                        label = context.getString(R.string.settings_version_label),
                        value = BuildConfig.VERSION_NAME,
                        description = context.getString(R.string.settings_version_description),
                        index = 0,
                        totalItems = 2
                    )

                    SettingsInfoRow(
                        label = context.getString(R.string.settings_source_code_label),
                        value = "",
                        description = context.getString(R.string.settings_source_code_description),
                        index = 1,
                        totalItems = 2,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                context.getString(R.string.url_github_repo).toUri())
                            context.startActivity(intent)
                        }
                    )
                }
            }
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String,
    description: String,
    index: Int,
    totalItems: Int,
    onClick: (() -> Unit)? = null
) {
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = getModularCornerShape(index, totalItems)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    lineHeight = 16.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (value.isNotEmpty()) {
                Spacer(Modifier.width(16.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
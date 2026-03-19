package dev.ilas.dithra.presentation.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays the processed image with optional export progress overlay.
 *
 * @param modifier Optional modifier for styling
 * @param processedBitmap Processed bitmap to display
 * @param isExporting Whether export operation is in progress
 * @param exportProgress Export progress (0.0 to 1.0)
 * @param onClick Optional callback when the image is clicked
 */
@Composable
fun ImageDisplaySection(
    modifier: Modifier = Modifier,
    processedBitmap: Bitmap?,
    isExporting: Boolean = false,
    exportProgress: Float = 0f,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            processedBitmap?.let { bitmap ->
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "Processed Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .let { 
                            if (onClick != null) {
                                it.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onClick
                                )
                            } else {
                                it
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            } ?: CircularProgressIndicator()
            
            if (isExporting) {
                ExportProgressOverlay(progress = exportProgress)
            }
        }
    }
}

/**
 * Overlay displaying export progress with animated indicator.
 * 
 * @param progress Export progress (0.0 to 1.0)
 * @param modifier Optional modifier for styling
 */
@Composable
private fun ExportProgressOverlay(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(64.dp),
                color = ProgressIndicatorDefaults.circularColor,
                strokeWidth = 8.dp,
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Preparing export...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
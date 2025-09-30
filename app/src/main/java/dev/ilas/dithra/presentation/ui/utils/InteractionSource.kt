package dev.ilas.dithra.presentation.ui.utils

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Interaction source that suppresses all interactions for embedded components.
 * 
 * Used to prevent touch conflicts when components are embedded within
 * other interactive elements (e.g., sliders inside toggle buttons).
 */
class NoInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction): Boolean = true
}
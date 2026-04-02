package com.musicai.ui.shared.components

import androidx.compose.runtime.Composable
import com.musicai.ui.theme.components.AppErrorState
import com.musicai.ui.theme.components.AppLoadingIndicator

/**
 * Handles the common Loading → Error → Content flow shared across multiple screens.
 *
 * @param isLoading Whether to show the loading indicator
 * @param error Error message to display with optional retry. Null means no error.
 * @param onRetry Callback for the retry button. Null hides the retry button.
 * @param content Composable content to show when not loading and no error
 */
@Composable
fun ContentStateWrapper(
    isLoading: Boolean,
    error: String?,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    when {
        isLoading -> AppLoadingIndicator()
        error != null -> AppErrorState(message = error, onRetry = onRetry)
        else -> content()
    }
}

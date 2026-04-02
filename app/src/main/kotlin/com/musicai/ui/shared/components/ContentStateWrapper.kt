package com.musicai.ui.shared.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.musicai.ui.shared.ErrorState

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
    error: ErrorState? = null,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    when {
        isLoading -> AppLoadingIndicator()
        error != null -> AppErrorState(message = stringResource(error.message), onRetry = onRetry)
        else -> content()
    }
}

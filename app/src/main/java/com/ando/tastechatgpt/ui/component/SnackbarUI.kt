package com.ando.tastechatgpt.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object SnackbarUI {
    val snackbarHostState = SnackbarHostState()

    @Composable
    fun ComposeUI() {
        SnackbarHost(hostState = snackbarHostState) {
            Snackbar(
                snackbarData = it,
//                contentColor = MaterialTheme.colorScheme.primary,
//                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }

    suspend fun showMessage(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ): SnackbarResult {
        return snackbarHostState.showSnackbar(message = message, duration = duration)
    }
}
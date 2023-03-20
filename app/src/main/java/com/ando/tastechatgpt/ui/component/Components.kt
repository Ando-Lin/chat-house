package com.ando.tastechatgpt.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    modifier: Modifier = Modifier,
    title: String,
    onBackClick: ()->Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBackIos, contentDescription = null)
            }
        },
    )
}





val topBarHeight = 40.dp









package com.ando.tastechatgpt.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableIconTextListItem(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    contentColor: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    ListItem(
        headlineText = {
            Text(text = text, color = contentColor)
        },
        leadingContent = {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        },
        modifier = modifier.clickable(onClick = onClick)
    )
}
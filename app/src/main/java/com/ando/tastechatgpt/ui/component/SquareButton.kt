package com.ando.tastechatgpt.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FilledTonalSquareIconButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20),
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    label: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication = rememberRipple(),//TODO: 修改为动画效果
                    role = Role.Button
                )
                .size(40.dp)
                .background(
                    color = containerColor,
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                icon()
            }
        }
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
            if (label != null) {
                label()
            }
        }
    }
}

@Preview
@Composable
private fun FilledTonalSquareButtonPrev() {
    FilledTonalSquareIconButton(
        onClick = {},
        label = { Text(text = "清空对话") },
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier
            )
        }
    )
}
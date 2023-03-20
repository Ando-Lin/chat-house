package com.ando.tastechatgpt.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TTextField(
    modifier: Modifier = Modifier,
    text: String,
    tip: String = "",
    maxLines: Int = 1,
    showIndicator: Boolean = false,
    onTextChange: (String) -> Unit,
    shape: Shape = RoundedCornerShape(10),
    interactionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    }
) {
    val textStyle = MaterialTheme.typography.bodyLarge
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        maxLines = maxLines,
        modifier = modifier
            .animateContentSize(),
        textStyle = textStyle.copy(color = colorScheme.onSurface),
        cursorBrush = SolidColor(colorScheme.primary),
        interactionSource = interactionSource,
    ) { content ->
        Box(
            modifier = Modifier
                .background(
                    color = colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = shape
                )
                .drawBehind {
                    if (!showIndicator) return@drawBehind
                    val lineWidth = with(density) { 0.5.dp.toPx() }
                    drawLine(
                        color = colorScheme.onSurface,
                        start = Offset(0f, this.size.height),
                        end = Offset(this.size.width, this.size.height),
                        strokeWidth = lineWidth
                    )
                }
        ) {
            Box(modifier = Modifier.padding(12.dp).align(Alignment.TopStart)) {
                content()
                if (text.isBlank()) {
                    Text(
                        text = tip,
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
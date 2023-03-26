package com.ando.tastechatgpt.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TTextField(
    modifier: Modifier = Modifier,
    text: String,
    tip: String = "",
    maxLines: Int = 1,
    showIndicator: Boolean = false,
    onTextChange: (String) -> Unit,
    textStyle: TextStyle = TextStyle.Default,
    colors: TTextFieldColors = TTextFieldColors.defaultColors(),
    shape: Shape = RoundedCornerShape(10),
    contentPadding: PaddingValues = PaddingValues(12.dp),
    interactionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    }
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        maxLines = maxLines,
        modifier = modifier,
        textStyle = textStyle.copy(color = colors.contentColor),
        cursorBrush = SolidColor(colorScheme.primary),
        interactionSource = interactionSource,
    ) { content ->
        Surface(
            color = colors.containerColor,
            shape = shape,
            modifier = Modifier
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
            Box(
                modifier = Modifier
                    .padding(contentPadding)
            ) {
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

data class TTextFieldColors(
    val contentColor: Color,
    val containerColor: Color
) {
    companion object {
        @Composable
        fun defaultColors(
            contentColor: Color = MaterialTheme.colorScheme.onSurface,
            containerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        ) =
            TTextFieldColors(contentColor = contentColor, containerColor = containerColor)
    }
}


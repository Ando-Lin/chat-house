package com.ando.tastechatgpt.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class MessageBubbleShape(private val cornerSize: Dp, private val triangle: Triangle):Shape {

    data class Triangle(
        val base: Dp,
        val height: Dp,
        val fromTop: Dp
    )

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return if (layoutDirection == LayoutDirection.Ltr){
            Outline.Generic(
                path = drawBubbleLtrPath(
                    size = size,
                    cornerSize = density.run { cornerSize.toPx() },
                    topOffset = density.run { triangle.fromTop.toPx() },
                    b = density.run { triangle.base.toPx() },
                    h = density.run { triangle.height.toPx() }
                )
            )
        }else{
            Outline.Generic(
                path = drawBubbleRtlPath(
                    size = size,
                    cornerSize = density.run { cornerSize.toPx() },
                    topOffset = density.run { triangle.fromTop.toPx() },
                    b = density.run { triangle.base.toPx() },
                    h = density.run { triangle.height.toPx() }
                )
            )
        }

    }

    private fun drawBubbleRtlPath(
        size: Size,
        cornerSize: Float,
        topOffset: Float,
        b: Float,
        h: Float
    ): Path {
        return Path().apply {
            //左上
            arcTo(
                rect = Rect(
                    left = 0.0f,
                    top = 0.0f,
                    right = cornerSize,
                    bottom = cornerSize
                ),
                startAngleDegrees = 180.0f,
                sweepAngleDegrees = 90.0f,  //逆时针扫过
                forceMoveTo = false
            )
            lineTo(x = size.width - cornerSize - h, y = 0f)
            // 右上
            arcTo(
                rect = Rect(
                    left = size.width - cornerSize - h,
                    top = 0f,
                    right = size.width - h,
                    bottom = cornerSize
                ),
                startAngleDegrees = 270.0f,
                sweepAngleDegrees = 90.0f,
                forceMoveTo = false
            )
            lineTo(x = size.width-h, y = topOffset - b/2)
            lineTo(x = size.width, y = topOffset)
            lineTo(x = size.width-h, y = topOffset + b/2)
            lineTo(x = size.width-h, y = size.height - cornerSize)
            // 右下
            arcTo(
                rect = Rect(
                    left = size.width - cornerSize - h,
                    top = size.height - cornerSize,
                    right = size.width - h,
                    bottom = size.height
                ),
                startAngleDegrees = 0.0f,
                sweepAngleDegrees = 90.0f,
                forceMoveTo = false
            )
            lineTo(x = cornerSize + h, y = size.height)
            // 左下
            arcTo(
                rect = Rect(
                    left = 0.0f,
                    top = size.height - cornerSize,
                    right = cornerSize,
                    bottom = size.height
                ),
                startAngleDegrees = 90.0f,
                sweepAngleDegrees = 90.0f,
                forceMoveTo = false
            )
            lineTo(x = 0.0f, y = cornerSize)
            close()
        }
    }

    private fun drawBubbleLtrPath(
        size: Size,
        cornerSize: Float,
        topOffset: Float,
        b: Float,
        h: Float
    ): Path {
        return Path().apply {
            //左上
            arcTo(
                rect = Rect(
                    left = h,
                    top = 0f,
                    right = cornerSize + h,
                    bottom = cornerSize
                ),
                startAngleDegrees = 180.0f,
                sweepAngleDegrees = 90.0f,  //逆时针扫过
                forceMoveTo = false
            )
            lineTo(x = size.width - cornerSize, y = 0f)
            // 右上
            arcTo(
                rect = Rect(
                    left = size.width - cornerSize,
                    top = 0f,
                    right = size.width,
                    bottom = cornerSize
                ),
                startAngleDegrees = 270.0f,
                sweepAngleDegrees = 90.0f,
                forceMoveTo = false
            )
            lineTo(x = size.width, y = size.height - cornerSize)
            // 右下
            arcTo(
                rect = Rect(
                    left = size.width - cornerSize,
                    top = size.height - cornerSize,
                    right = size.width,
                    bottom = size.height
                ),
                startAngleDegrees = 0.0f,
                sweepAngleDegrees = 90.0f,
                forceMoveTo = false
            )
            lineTo(x = cornerSize + h, y = size.height)
            // 左下
            arcTo(
                rect = Rect(
                    left = h,
                    top = size.height - cornerSize,
                    right = cornerSize + h,
                    bottom = size.height
                ),
                startAngleDegrees = 90.0f,
                sweepAngleDegrees = 90.0f,
                forceMoveTo = false
            )
            lineTo(x = h, y = topOffset + b/2)
            lineTo(x = 0.0f, y = topOffset)
            lineTo(x = h, y = topOffset - b/2)
            lineTo(x = h, y = cornerSize)
            close()

        }
    }
}


@Preview()
@Composable
private fun PreviewBubbleShape() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface {
            Box(modifier = Modifier
                .size(width = 80.dp, height = 50.dp)
                .graphicsLayer {
                    shape = MessageBubbleShape(
                        cornerSize = 10.dp,
                        triangle = MessageBubbleShape.Triangle(
                            base = 15.dp,
                            height = 8.dp,
                            fromTop = 25.dp
                        )
                    )
                    clip = true
                }
                .background(color = Color.Blue)
            )
        }

    }
}

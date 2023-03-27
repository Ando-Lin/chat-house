package com.ando.chathouse.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

@Composable
fun CycleAnimation(
    modifier: Modifier = Modifier,
    iconRes: Int?,
    duration: Int = 1000,
    isClockwise: Boolean = true,
) {
    if (iconRes==null) return
    val infiniteTransition = rememberInfiniteTransition()
    val signal = if(isClockwise) 1 else -1
    val floatState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = signal * 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Image(
        imageVector = ImageVector.vectorResource(id = iconRes),
        contentDescription = null,
        modifier = modifier
            .graphicsLayer {
                this.rotationZ = floatState.value
            }
    )
}
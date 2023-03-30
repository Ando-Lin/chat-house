package com.ando.chathouse.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.ando.chathouse.R

@Composable
fun Logo(modifier: Modifier = Modifier) {
    val color1 = MaterialTheme.colorScheme.primary
    val colors2 = MaterialTheme.colorScheme.primaryContainer
    Box(modifier = modifier.wrapContentSize(Alignment.Center)) {
        Box(modifier = Modifier
            .align(Alignment.BottomCenter)
            .width(40.dp)
            .height(14.dp)
            .offset(y = (-10).dp, x = 2.dp)
            .background(brush = Brush.horizontalGradient(colors = listOf(colors2,color1)), shape = CircleShape)
        )
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            alpha = 0.95f,
        )

    }
}
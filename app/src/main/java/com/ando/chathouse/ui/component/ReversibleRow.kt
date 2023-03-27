package com.ando.chathouse.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun ReversibleRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    reverseLayout: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val originDirection = LocalLayoutDirection.current
    val direction = when {
        reverseLayout -> when (originDirection) {
            LayoutDirection.Rtl -> LayoutDirection.Ltr
            else -> LayoutDirection.Rtl
        }
        else -> originDirection
    }
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Row(modifier, horizontalArrangement, verticalAlignment) {
            CompositionLocalProvider(LocalLayoutDirection provides originDirection) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReversibleRowPrev() {
    Column(Modifier.fillMaxWidth()) {
        ReversibleRow(Modifier.fillMaxWidth()) {
            Text(text = "11")
            Text(text = "22")
        }
        ReversibleRow(reverseLayout = true, modifier = Modifier.fillMaxWidth()) {
            Text(text = "33")
            Text(text = "44")
        }
    }
}
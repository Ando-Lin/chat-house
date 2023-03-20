package com.ando.tastechatgpt.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.ando.tastechatgpt.ui.theme.TasteChatGPTTheme
import kotlin.math.roundToInt


@Composable
fun TPopup(
    modifier: Modifier = Modifier,
    visible: Boolean,
    offset: IntOffset,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return
    Popup(
        alignment = Alignment.TopStart,
        offset = offset,
        onDismissRequest = onDismissRequest
    ) {
        Surface(shadowElevation = 1.dp, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TPopupPrev() {
    TasteChatGPTTheme {
        var visible by remember {
            mutableStateOf(false)
        }
        var offset by remember {
            mutableStateOf(Offset.Zero)
        }
        val density = LocalDensity.current
        val intOffset = lazy {
//            with(density){
//                IntOffset(x=offset.x.toDp().value.toInt(),y=offset.y.toDp().value.toInt())
//            }
            IntOffset(x = offset.x.roundToInt(), y = offset.y.roundToInt())
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                Text(
                    text = "开启弹窗: $offset",
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures {
                                offset = it
                                visible = true
                            }
                        }
                        .size(170.dp, 40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        )
                )
                TPopup(
                    offset = intOffset.value,
                    visible = visible,
                    onDismissRequest = { visible = false }
                ) {
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "选项一")
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "选项一")
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "选项一")
                    }
                }
            }
        }
    }
}
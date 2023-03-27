package com.ando.tastechatgpt.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ando.tastechatgpt.ext.toAnnotatedString
import com.ando.tastechatgpt.ext.withMutableInteractionSource
import kotlinx.coroutines.delay

data class BubbleTextUiState(
    val text: ()->String,
    val isMe: Boolean,
    val selected: Boolean,
    val reading: Boolean = false,
    private val editModeState: State<Boolean>,
    private val multiSelectModeState: State<Boolean>
) {
    internal var checked by mutableStateOf(false)

    @Composable
    fun checked() = rememberSaveable() { mutableStateOf(false) }
    val editMode: Boolean by editModeState
    val multiSelectMode: Boolean by multiSelectModeState
}

@Composable
private fun bubbleTextColor(uiState: BubbleTextUiState): Pair<Color, Color> {
    val colorScheme = MaterialTheme.colorScheme
    val textColor: Color
    val bubbleColor: Color
    if (uiState.editMode) {
        if (uiState.selected) {
            bubbleColor = colorScheme.primaryContainer
            textColor = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        } else {
            bubbleColor = colorScheme.surfaceColorAtElevation(2.dp)
            textColor = colorScheme.contentColorFor(bubbleColor).copy(alpha = 0.5f)
        }
    } else {
        if (uiState.isMe) {
            bubbleColor = colorScheme.primary
            textColor = colorScheme.onPrimary
        } else {
            bubbleColor = colorScheme.surfaceVariant
            textColor = colorScheme.onSurfaceVariant
        }
    }
    return bubbleColor to textColor
}


/**
 * 扩展了功能的BubbleText. checked状态的改变由内部完成
 */
@Composable
fun ExtendedBubbleText(
    modifier: Modifier = Modifier,
    uiState: BubbleTextUiState,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val (bubbleColor, textColor) = bubbleTextColor(uiState = uiState)
    var checked by uiState.checked()
    //关闭多选时取消checked
    LaunchedEffect(uiState.multiSelectMode) {
        if (!uiState.multiSelectMode) {
            checked = false
        }
    }
    //当selected变化时说明执行了操作，需要取消checked状态
    LaunchedKeyEffect(uiState.selected){
        checked = false
    }



    Box {

        BubbleText(
            modifier = modifier,
            text = uiState.text,
            textColor = textColor,
            containerColor = bubbleColor,
            shapeReverse = !uiState.isMe,
            reading = uiState.reading,
            onLongClick = onLongClick,
            onClick = {
                checked = uiState.multiSelectMode && !checked
                onClick()
            }
        )


        AnimatedVisibility(
            visible = checked, modifier = Modifier
                .align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 65.dp, height = 40.dp)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(100))
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(100)
                    )
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

    }
}

@Composable
fun BubbleText(
    modifier: Modifier = Modifier,
    text: ()->String,
    textColor: Color = LocalContentColor.current,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    shapeReverse: Boolean = false,
    reading: Boolean = false,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    //TODO: 将annotatedString解析有效信息并设置为chip样式
    val shape = if (!shapeReverse) {
        RoundedCornerShape(20.dp, 5.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(5.dp, 20.dp, 20.dp, 20.dp)
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    Surface(color = containerColor,
        shape = shape,
        modifier = modifier
            .wrapContentSize()
//            .indication(interactionSource, rememberRipple(bounded = false))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongClick()
                    },
                    onTap = {
                        onClick()
                    },
                    onPress = { withMutableInteractionSource(it, interactionSource) }
                )
            }
    ) {
        if (reading){
            //滚动时使用
            var newText by rememberSaveable {
                mutableStateOf("")
            }
            val receiveText = text()
            LaunchedEffect(receiveText){
                val start = receiveText.indexOf(newText) + newText.length
                for (i in start until receiveText.length){
                    delay(50)
                    newText += receiveText[i]
                }
            }
            TText(
                text = newText,
                interactionSource = interactionSource,
                textColor = textColor,
                onLongClick = onLongClick,
                onClick = onClick
            )
        }else{
            TText(
                text = text(),
                interactionSource = interactionSource,
                textColor = textColor,
                onLongClick = onLongClick,
                onClick = onClick
            )
        }

    }
}

@Composable
private fun TText(
    text: String,
    interactionSource:MutableInteractionSource,
    textColor:Color,
    onLongClick: () -> Unit,
    onClick: ()->Unit,
) {
    val layoutResult = remember {
        mutableStateOf<TextLayoutResult?>(null)
    }
    val onClickText = { offset: Int -> /*TODO: 点击文字特殊tag做出响应*/ }
    Text(
        text = text.toAnnotatedString(),
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongClick()
                    },
                    onTap = {
                        onClick()
                        layoutResult.value?.let { layout ->
                            onClickText(layout.getOffsetForPosition(it))
                        }
                    },
                    onPress = { withMutableInteractionSource(it, interactionSource) }
                )
            },
        style = MaterialTheme.typography.bodyLarge.copy(
            color = textColor, textAlign = TextAlign.Start
        ),
        onTextLayout = {
            layoutResult.value = it
        })
}




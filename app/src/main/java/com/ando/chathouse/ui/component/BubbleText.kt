package com.ando.chathouse.ui.component

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
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ando.chathouse.constant.WRITE_DB_TIME_THRESHOLD
import com.ando.chathouse.constant.WRITE_DB_TOKEN_THRESHOLD
import com.ando.chathouse.ext.toAnnotatedString
import com.ando.chathouse.ext.withMutableInteractionSource
import kotlinx.coroutines.delay

data class BubbleTextUiState(
    val id: Int,
    val text: () -> String,
    val isMe: Boolean,
    val selected: Boolean,
    val reading: Boolean = false,
    val checkedMap: SnapshotStateMap<Int, Unit>,
    private val editModeState: State<Boolean>,
    private val multiSelectModeState: State<Boolean>
) {
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
            bubbleColor = colorScheme.inverseSurface.copy(alpha = 0.1f)
            textColor = colorScheme.onSurface.copy(alpha = 0.5f)
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
    onClick: () -> Unit,
    onChecked: (Int) -> Unit,
) {
    val (bubbleColor, textColor) = bubbleTextColor(uiState = uiState)

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
                if (uiState.multiSelectMode) {
                    onChecked(uiState.id)
                } else {
                    onClick()
                }
            }
        )

        if (uiState.multiSelectMode) {
            val visible by remember {
                derivedStateOf { (uiState.checkedMap.containsKey(uiState.id)) }
            }
            AnimatedVisibility(
                visible = visible, modifier = Modifier
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
}

@Composable
fun BubbleText(
    modifier: Modifier = Modifier,
    text: () -> String,
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
        if (reading) {
            //流式打印字符.用saveble防止显示异常
            var newText by rememberSaveable {
                mutableStateOf("")
            }
            val receiveText = text()
            LaunchedEffect(receiveText) {
                //根据start和receiveText的长度调整速度或者增量
                //待打印的数量较多时,增加charDelta,降低delay.反之减少charDelta增大delay
                var start = receiveText.indexOf(newText) + newText.length
                val len = receiveText.length - start
                //每个字符的最长写入时间，超过则触发更新数据库进一步增长len导致延迟问题
                val perCharTime =
                    when {
                        len > 0 -> WRITE_DB_TIME_THRESHOLD / len
                        else -> 0
                    }
                //每个token的最少字符数。例如中文：1~2字=1个token，则理想情况下len（字符数）~=token数，此时应一个字符一个字符的打印
                val perTokenChar =
                    when {
                        len < WRITE_DB_TOKEN_THRESHOLD -> len / WRITE_DB_TOKEN_THRESHOLD
                        else -> 1
                    }
                while (newText.length != receiveText.length) {
                    val endIndex =
                        when {
                            start + perTokenChar >= receiveText.length -> receiveText.length
                            else -> perTokenChar + start
                        }
                    newText += receiveText.subSequence(start, endIndex)
                    start += perTokenChar
                    delay(perCharTime)
                }
            }
            TText(
                text = buildAnnotatedString { append(newText) },
                interactionSource = interactionSource,
                textColor = textColor,
                onLongClick = onLongClick,
                onClick = onClick
            )
        } else {
            TText(
                text = text().toAnnotatedString(),
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
    text: AnnotatedString,
    interactionSource: MutableInteractionSource,
    textColor: Color,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
) {
    val layoutResult = remember {
        mutableStateOf<TextLayoutResult?>(null)
    }
    val onClickText = { offset: Int -> /*TODO: 点击文字特殊tag做出响应*/ }
    Text(
        text = text,
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




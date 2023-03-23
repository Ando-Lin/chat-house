@file:OptIn(ExperimentalLayoutApi::class)

package com.ando.tastechatgpt.ui.component.exclusive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.ext.withCondition
import com.ando.tastechatgpt.ui.component.TTextField


data class ChatScreenBottomBarUiState(
    val text: String = "",
    val editModeState: State<Boolean>,
    val multiSelectModeState: State<Boolean>,
){
    val editMode: Boolean by editModeState
    val multiSelectMode: Boolean by multiSelectModeState
}

@Composable
fun ChatScreenExtendedBottomBar(
    modifier: Modifier = Modifier,
    uiState: ChatScreenBottomBarUiState,
    onSend: (fromRight: Boolean, content: String) -> Unit,
    onTextChange: (String) -> Unit,
    onClickCarry: () -> Unit,
    onClickExclude: () -> Unit,
    onClickDelete: () -> Unit
) {

    val visible by remember {
        derivedStateOf{uiState.multiSelectMode && uiState.editMode}
    }
    Column(modifier = modifier) {
        AnimatedVisibility(visible = visible) {
            FlowRow {
                TextButton(onClick = onClickCarry) {
                    Text(text = stringResource(id = R.string.carry_selected))
                }
                TextButton(onClick = onClickExclude) {
                    Text(text = stringResource(id = R.string.exclude_selected))
                }
                TextButton(onClick = onClickDelete) {
                    Text(text = stringResource(id = R.string.delete_selected))
                }
            }
        }
        ChatScreenBottomBar(
            modifier = Modifier,
            text = uiState.text,
            editMode = uiState.editMode,
            onSend = onSend,
            onTextChange = onTextChange,
        )
    }
}


/**
 * 双输入框->对话样本学习
 */
@Composable
fun ChatScreenBottomBar(
    modifier: Modifier = Modifier,
    text: String,
    editMode: Boolean,
    onSend: (fromRight: Boolean, content: String) -> Unit,
    onTextChange: (String) -> Unit,
    rightInteractionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    },
    leftInteractionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    },
) {
    val isLeftFocused by leftInteractionSource.collectIsFocusedAsState()
    val isRightFocused by rightInteractionSource.collectIsFocusedAsState()
    //同时聚焦或者同时没聚焦时为true
    val com = !(isLeftFocused xor isRightFocused)
    Row(
        modifier = modifier
            .padding(10.dp)
    ) {
        if (editMode) {
            InputAndSend(
                text = text,
                tip = stringResource(id = R.string.they_say_),
                onSend = { onSend(false, it) },
                onTextChange = onTextChange,
                interactionSource = leftInteractionSource,
                showButton = !com,
                reverseLayout = true,
                modifier = Modifier
                    .withCondition(!com and isRightFocused) {
                        size(0.dp)
                    }
                    .withCondition(com) {
                        weight(1f)
                    }
                    .animateContentSize()
            )
            Spacer(modifier = Modifier.withCondition(com) { width(10.dp) })
        }
        InputAndSend(
            text = text,
            tip = stringResource(id = R.string.i_say_),
            onSend = { onSend(true, it) },
            showButton = !editMode or !com,
            onTextChange = onTextChange,
            interactionSource = rightInteractionSource,
            modifier = Modifier
                .withCondition(!com and isLeftFocused) {
                    size(0.dp)
                }
                .withCondition(com) {
                    weight(1f)
                }
                .animateContentSize()
        )
    }
}

@Composable
fun InputAndSend(
    modifier: Modifier = Modifier,
    showButton: Boolean = true,
    text: String,
    tip: String = "",
    onSend: (String) -> Unit,
    onTextChange: (String) -> Unit,
    reverseLayout: Boolean = false,
    interactionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    }
) {

    val buttonModifier = Modifier
        .withCondition(!showButton) { width(0.dp) }
        .withCondition(!reverseLayout) {
            padding(start = 10.dp)
        }
        .withCondition(reverseLayout) {
            padding(end = 10.dp)
        }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val textFieldModifier = Modifier
            .weight(1f)
        if (!reverseLayout) {
            //输入框
            TTextField(
                text = text,
                onTextChange = onTextChange,
                maxLines = 6,
                modifier = textFieldModifier,
                tip = tip,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(20.dp)
            )

            //发送按钮
            Button(
                onClick = { onSend(text) },
                modifier = buttonModifier
            ) {
                Text(
                    text = stringResource(id = R.string.send), modifier = Modifier
                )
            }
        } else {
            //发送按钮
            Button(
                onClick = { onSend(text) },
                modifier = buttonModifier
            ) {
                Text(
                    text = stringResource(id = R.string.send), modifier = Modifier
                )
            }
            //输入框
            TTextField(
                text = text,
                onTextChange = onTextChange,
                maxLines = 6,
                modifier = textFieldModifier,
                tip = tip,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
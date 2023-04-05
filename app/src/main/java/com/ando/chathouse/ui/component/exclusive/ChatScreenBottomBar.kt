@file:OptIn(ExperimentalLayoutApi::class)

package com.ando.chathouse.ui.component.exclusive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ando.chathouse.R
import com.ando.chathouse.ext.withCondition
import com.ando.chathouse.ui.component.ReversibleRow
import com.ando.chathouse.ui.component.TTextField


data class ChatScreenBottomBarUiState(
    val text:()->String = {""},
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
    onGoOn: ()->Unit,
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
            onGoOn = onGoOn
        )
    }
}


/**
 * 双输入框
 */
@Composable
fun ChatScreenBottomBar(
    modifier: Modifier = Modifier,
    text: ()->String,
    editMode: Boolean,
    onSend: (fromRight: Boolean, content: String) -> Unit,
    onGoOn: ()->Unit,
    onTextChange: (String) -> Unit,
    rightInteractionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    },
    leftInteractionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    },
) {
    val hasText = remember(text) {
        derivedStateOf { text().isNotBlank() }
    }
    //按钮文字
    val buttonText = stringResource(id = R.string.send)
    //当其中一个聚焦时隐藏另一个
    val rightFocused = rightInteractionSource.collectIsFocusedAsState()
    val leftFocused = leftInteractionSource.collectIsFocusedAsState()
    val com = rightFocused.value xor leftFocused.value
    Column(
        modifier = modifier
            .padding(10.dp)
    ) {
        if (editMode && (!com || leftFocused.value)) {
            InputAndSend(
                text = text,
                tip = stringResource(id = R.string.they_say_),
                onSend = { onSend(false, it) },
                onTextChange = onTextChange,
                interactionSource = leftInteractionSource,
                reverseLayout = true,
                modifier = Modifier,
                buttonText = buttonText,
                enableButton = hasText.value
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if(!com || !leftFocused.value){
            InputAndSend(
                text = text,
                tip = stringResource(id = R.string.i_say_),
                onSend = { onSend(true, it) },
                onTextChange = onTextChange,
                interactionSource = rightInteractionSource,
                modifier = Modifier,
                buttonText = buttonText,
                enableButton = hasText.value,
            )
        }
    }
}

@Composable
fun InputAndSend(
    modifier: Modifier = Modifier,
    showButton: Boolean = true,
    text: ()->String,
    buttonText: String,
    tip: String = "",
    enableButton: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
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

    ReversibleRow(modifier = modifier, verticalAlignment = Alignment.CenterVertically, reverseLayout = reverseLayout) {
        val textFieldModifier = Modifier
            .weight(1f)
        //输入框
        TTextField(
            text = text(),
            onTextChange = onTextChange,
            maxLines = 6,
            modifier = textFieldModifier,
            tip = tip,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(20.dp)
        )
        //发送/继续按钮
        Button(
            onClick = { onSend(text()) },
            modifier = buttonModifier,
            enabled = enableButton,
            colors = colors,
        ) {
            Text(
                text = buttonText, modifier = Modifier
            )
        }
    }
}
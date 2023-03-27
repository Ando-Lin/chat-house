package com.ando.chathouse.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ando.chathouse.R
import com.ando.chathouse.ui.theme.ChatHouseTheme

@Composable
fun SimpleAlertDialog(
    dialogVisible: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = onCancel,
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel, modifier = Modifier.wrapContentSize()) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            text = content,
            properties = properties,
            title = title
        )
    }
}


@Composable
fun AlertDialogForWarningSave(
    dialogVisible: Boolean,
    onChoiseNotSave: () -> Unit,
    onChoiseSave: () -> Unit,
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = onChoiseSave,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onChoiseNotSave, modifier = Modifier.wrapContentSize()) {
                    Text(text = stringResource(id = R.string.not_save))
                }
            },
            text = content,
            properties = properties,
            title = title
        )
    }
}


@Composable
fun TDialog(
    modifier: Modifier = Modifier,
    dialogVisible: Boolean = true,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (dialogVisible) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 3.dp,
                modifier = modifier
            ) {
                Column {
                    content()
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogForStringInput(
    modifier: Modifier = Modifier,
    dialogVisible: Boolean = true,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit,
    initText: String,
    label: String? = null,
    placeholder: String? = null,
) {
    val text = rememberSaveable(initText) {
        mutableStateOf(initText)
    }

    SimpleAlertDialog(
        dialogVisible = dialogVisible,
        onCancel = onCancel,
        onConfirm = { onConfirm(text.value) }
    ) {
        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = if (label != null) { { Text(text = label) } } else null,
            placeholder = if (placeholder!=null){{ Text(text = placeholder)}} else null,
            modifier = modifier
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ListDialogPre() {
    ChatHouseTheme {
        TDialog(dialogVisible = true, onDismissRequest = {}) {
            ListItem(
                headlineText = {
                    Text(text = "复制")
                },
                leadingContent = {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                },
            )
        }
        Surface(modifier = Modifier.fillMaxSize()) {

        }
    }
}

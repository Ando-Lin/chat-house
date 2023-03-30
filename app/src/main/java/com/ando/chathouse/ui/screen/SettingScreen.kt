@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.ando.chathouse.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ando.chathouse.R
import com.ando.chathouse.domain.pojo.*
import com.ando.chathouse.ui.component.*
import com.ando.chathouse.ui.screen.state.SettingsScreenViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    backAction: () -> Unit, viewModel: SettingsScreenViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val originalApiKey by uiState.apiKeyState()
    val text = rememberSaveable {
        mutableStateOf("")
    }
    LaunchedKeyEffect(originalApiKey){
        if (text.value.isBlank() && !originalApiKey.isNullOrBlank()){
            text.value = originalApiKey!!
        }
    }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember {
        MutableInteractionSource()
    }
    var dialogState by remember {
        mutableStateOf(false)
    }
    val updateAndBack =
        {
            if (originalApiKey != text.value)
                viewModel.updateApiKey(text.value)
            backAction()
        }

    BackHandler {
        updateAndBack()
    }
    LaunchedEffect(Unit) {
        interactionSource.interactions.collect {
            if (it is FocusInteraction.Unfocus) {
                viewModel.updateApiKey(text.value)
            }
        }
    }
    Scaffold(
        topBar = {
            Surface(shadowElevation = 1.dp) {
                TopBar(onClickBack = updateAndBack, title = stringResource(id = R.string.setting))
            }
        },
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { focusManager.clearFocus() }
        }
    ) { paddingValues ->
        Surface(
            color = backgroundColor,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            val enableHelp = uiState.enableHelpCollectingState().value
            ScreenContent(
                text = text.value,
                enableHelp = enableHelp ?: false,
                onTextChange = { text.value = it },
                onSwitchEnableHelp = {
                    if (enableHelp == true) {
                        viewModel.updateEnableHelp(it)
                    } else {
                        dialogState = true
                    }
                },
                interactionSource = interactionSource,
            )
        }
    }
    SimpleAlertDialog(
        dialogVisible = dialogState,
        onCancel = { dialogState = false;viewModel.updateEnableHelp(false) },
        onConfirm = { dialogState = false;viewModel.updateEnableHelp(true) },
        title = { Text(text = stringResource(id = R.string.privacy_collection_statement)) }
    ) {
        SelectionContainer {
            Text(text = stringResource(id = R.string.privacy_collection_statement_content_))
        }
    }
}


@Composable
private fun TopBar(
    onClickBack: () -> Unit,
    title: String
) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(imageVector = Icons.Default.ArrowBackIos, contentDescription = null)
            }
        }
    )
}

@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    text: String,
    enableHelp: Boolean,
    onTextChange: (String) -> Unit,
    onSwitchEnableHelp: (Boolean) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val itemModifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(Alignment.Top)
        .background(color = MaterialTheme.colorScheme.surface)
        .padding(horizontal = 20.dp)
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleSmall) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = itemModifier
            ) {
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    //openai apikey
                    Text(
                        text = stringResource(id = R.string.openai_api_key),
//                    style = MaterialTheme.typography.titleSmall,
//                    modifier = Modifier.padding(top = 10.dp)
                    )
                }
                TTextField(
                    text = text,
                    onTextChange = onTextChange,
                    tip = "xxxxxxxxxxxxxxxxxxxxx",
                    colors = TTextFieldColors.defaultColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    maxLines = 3,
                    interactionSource = interactionSource
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = itemModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.help_collecting_error),

                    modifier = Modifier.scrollable(
                        rememberScrollState(),
                        orientation = Orientation.Vertical
                    )
                )
                Switch(
                    checked = enableHelp,
                    onCheckedChange = onSwitchEnableHelp,
                    modifier = Modifier.scale(0.8f)
                )
            }
        }
    }

}


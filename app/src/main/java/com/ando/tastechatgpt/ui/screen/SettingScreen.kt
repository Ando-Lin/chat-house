@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.ando.tastechatgpt.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.domain.pojo.*
import com.ando.tastechatgpt.ui.component.*
import com.ando.tastechatgpt.ui.screen.state.SettingsScreenViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    backAction: () -> Unit, viewModel: SettingsScreenViewModel = hiltViewModel()
) {
    val backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val originalApiKey = viewModel.apiKey.collectAsState()
    val text = remember(originalApiKey) {
        mutableStateOf(originalApiKey.value ?: "")
    }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember {
        MutableInteractionSource()
    }
    LaunchedEffect(Unit) {
        interactionSource.interactions.collect {
            if (it is FocusInteraction.Unfocus) {
                viewModel.writeToProfile(viewModel.key, text.value.trim())
            }
        }
    }
    Scaffold(
        topBar = {
            Surface(shadowElevation = 1.dp) {
                SimpleTopBar(onBackClick = backAction, title = stringResource(id = R.string.setting))
            }
        },
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { focusManager.clearFocus() }
        }
    ) { paddingValues ->
        Surface(
            color = backgroundColor, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            ScreenContent(
                text = text.value,
                onTextChange = { text.value = it },
                interactionSource = interactionSource,
            )
        }
    }
}

@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Column(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp)
        ) {
            Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(6.dp))
                //openai apikey
                Text(
                    text = stringResource(id = R.string.openai_api_key),
                    style = MaterialTheme.typography.titleSmall,
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
                maxLines = 1,
                interactionSource = interactionSource
            )
        }
    }


}

@Preview(showBackground = true)
@Composable
private fun Prev() {
    val backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    Surface(color = backgroundColor, modifier = Modifier.fillMaxSize()) {
        ScreenContent(text = "", onTextChange = {})
    }
}

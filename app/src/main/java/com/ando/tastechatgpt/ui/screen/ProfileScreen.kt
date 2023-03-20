package com.ando.tastechatgpt.ui.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.ui.component.AlertDialogForWarningSave
import com.ando.tastechatgpt.ui.component.AvatarImage
import com.ando.tastechatgpt.ui.component.SnackbarUI
import com.ando.tastechatgpt.ui.component.TTextField
import com.ando.tastechatgpt.ui.screen.state.ProfileViewModel
import com.ando.tastechatgpt.ui.theme.TasteChatGPTTheme
import com.skydoves.cloudy.Cloudy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(), backAction: () -> Unit) {
    val uiState = viewModel.screenUiState
    val tempUser = uiState.tempUser
    val message = uiState.message
    var dialogState by remember { mutableStateOf(false) }

    //显示消息
    LaunchedEffect(message){
        if (message.isNotBlank()){
            SnackbarUI.showMessage(message)
        }
    }

    //用于选择图片媒体的启动器.
    val resultLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?:return@rememberLauncherForActivityResult
            val newUri = viewModel.copyPictureToCacheFolder(uri)
            Log.i(TAG, "ProfileScreen: uri=$uri newUri=$newUri")
            viewModel.updateTempUser(tempUser.copy(avatar = newUri))
        }

    //若更新名称和描述后为保存则弹窗提示，否则直接返回
    val checkAndBack = {
        if (uiState.isModified){
            dialogState = true
        }else{
            backAction()
        }
    }

    //修改返回键操作
    BackHandler {
        checkAndBack()
    }

    //弹窗询问是否保存
    AlertDialogForWarningSave(
        dialogVisible = dialogState,
        onChoiseNotSave = { backAction() },
        onChoiseSave = { viewModel.saveUser();backAction() },
        onDismissRequest = { dialogState = false }) {
        Text(text = stringResource(id = R.string.unsaved_warning))
    }


    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.role),
                onClickBack = checkAndBack,
                onClickSave = viewModel::saveUser)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeadAndAvatar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onClickAvatar = {
                    resultLauncher.launch("image/*")
                },
                avatar = tempUser.avatar //TODO: 更换默认头像,
            )
            TextAndInput(
                title = stringResource(id = R.string.name),
                description = stringResource(id = R.string.name_description),
                content = tempUser.name,
                tip = stringResource(id = R.string.input_on_here),
                onTextChange = { viewModel.updateTempUser(tempUser.copy(name = it)) },
                modifier = Modifier.padding(10.dp)
            )
            TextAndInput(
                title = stringResource(id = R.string.role),
                description = stringResource(id = R.string.role_description),
                content = tempUser.description,
                tip = stringResource(R.string.input_on_here),
                onTextChange = {viewModel.updateTempUser(tempUser.copy(description = it))},
                maxLines = 10,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    onClickBack: () -> Unit,
    onClickSave: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        actions = {
            IconButton(onClick = onClickSave) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(id = R.string.save)
                )
            }
        }
    )
}

@Composable
fun HeadAndAvatar(
    modifier: Modifier = Modifier,
    avatar: Uri?,
    onClickAvatar: () -> Unit
) {
    val backgroundImage = avatar?:R.drawable.default_avatar
    Box(
        modifier = modifier
    ) {
        Cloudy(
            radius = 25, modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.TopCenter)
        ) {
            AsyncImage(
                model = backgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Row(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomStart)
                .clickable(onClick = onClickAvatar)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            AvatarImage(
                model = avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Text(
                text = stringResource(id = R.string.click_to_change_avatar),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.Bottom)
            )
        }
    }
}

@Composable
fun TextAndInput(
    modifier: Modifier = Modifier,
    title: String,
    tip: String = "",
    description: String,
    content: String,
    maxLines: Int = 1,
    onTextChange: (String) -> Unit
) {
    val basicHeight = 30.dp
    val minHeight = if (maxLines==1) basicHeight else basicHeight*maxLines/2
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(text = description, style = MaterialTheme.typography.bodySmall)
        TTextField(
            text = content,
            onTextChange = onTextChange,
            tip = tip,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .padding(top = 10.dp),
            maxLines = maxLines
        )
    }
}




@Composable
private fun UniformDivider() {
    Divider(
        modifier = Modifier.fillMaxWidth(0.7f),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Preview
@Composable
fun ProfileScreenPrev() {
    TasteChatGPTTheme {
//        ProfileScreen()
    }
}

private const val TAG = "ProfileScreen"

@file:OptIn(ExperimentalFoundationApi::class)

package com.ando.tastechatgpt.ui.screen

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.domain.pojo.User
import com.ando.tastechatgpt.ui.component.*
import com.ando.tastechatgpt.ui.screen.state.ProfileExtraSettingUiState
import com.ando.tastechatgpt.ui.screen.state.ProfileViewModel
import com.ando.tastechatgpt.ui.theme.TasteChatGPTTheme
import com.ando.tastechatgpt.util.Utils
import com.skydoves.cloudy.Cloudy
import com.skydoves.cloudy.internals.render.RenderScriptToolkit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    backAction: () -> Unit,
    isDialogMode: Boolean = false
) {
    val uiState = viewModel.screenUiState
    val tempUser = uiState.tempUser
    val message = uiState.message
    var dialogState by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState()
    val title = when (pagerState.currentPage) {
        0 -> stringResource(id = R.string.role_settings)
        else -> stringResource(id = R.string.extra_settings)
    }

    //显示消息
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            SnackbarUI.showMessage(message)
        }
    }

    //用于选择图片媒体的启动器.
    val resultLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@rememberLauncherForActivityResult
            val newUri = viewModel.copyPictureToCacheFolder(uri)
            Log.i(TAG, "ProfileScreen: uri=$uri newUri=$newUri")
            viewModel.updateTempUser(tempUser.copy(avatar = newUri))
        }

    //若更新名称和描述后为保存则弹窗提示，否则直接返回
    val checkAndBack = {
        if (uiState.isModified) {
            dialogState = true
        } else {
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
                title = title,
                showSave = pagerState.currentPage==0,
                isModified = uiState.isModified,
                isDialogMode = isDialogMode,
                onClickBack = checkAndBack,
                onClickSave = viewModel::saveUser
            )
        }
    ) { paddingValues ->
        VerticalPager(
            pageCount = 2,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            state = pagerState,
            beyondBoundsPageCount = 2
        ) { page ->
            val itemModifier = Modifier.fillMaxSize()
            when (page) {
                0 -> CommonSettings(
                    tempUser = tempUser,
                    resultLauncher = resultLauncher,
                    updateTempUser = viewModel::updateTempUser,
                    modifier = itemModifier
                )
                1 -> ExtraSettings(
                    uiState = viewModel.extraUiState,
                    onRoleGuideCheckedChange = viewModel::updateRoleGuideEnableState,
                    onReminderModeCheckedChange = viewModel::updateReminderModeEnableState,
                    onReminderInputComplete = viewModel::updateReminder,
                    modifier = itemModifier
                )
            }
        }
    }
}


@Composable
fun ProfileFullScreenDialog() {
    Dialog(onDismissRequest = { /*TODO*/ }) {
        TODO("未完成简介全屏对话框")
    }
}


@Composable
private fun CommonSettings(
    modifier: Modifier = Modifier,
    tempUser: User,
    resultLauncher: ManagedActivityResultLauncher<String, Uri?>,
    updateTempUser: (User) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val itemModifier = Modifier.padding(horizontal = 20.dp)
        HeadAndAvatar(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            onClickAvatar = {
                resultLauncher.launch("image/*")
            },
            avatar = tempUser.avatar
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextAndInput(
            title = stringResource(id = R.string.name),
            description = stringResource(id = R.string.name_description),
            content = tempUser.name,
            onTextChange = { updateTempUser(tempUser.copy(name = it)) },
            modifier = itemModifier
        )

        TextAndInput(
            title = stringResource(id = R.string.role_description),
            description = stringResource(id = R.string.assign_model_play_role),
            content = tempUser.description,
            onTextChange = { updateTempUser(tempUser.copy(description = it)) },
            maxLines = 10,
            modifier = itemModifier
        )
    }
}

@Composable
private fun ExtraSettings(
    modifier: Modifier = Modifier,
    uiState: ProfileExtraSettingUiState,
    onRoleGuideCheckedChange: (Boolean) -> Unit,
    onReminderModeCheckedChange: (Boolean) -> Unit,
    onReminderInputComplete: (String) -> Unit,
    interactionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    }
) {
    var reminder by rememberSaveable(uiState.reminder) {
        mutableStateOf(uiState.reminder)
    }
    val focusedAsState = interactionSource.collectIsFocusedAsState()
    LaunchedEffect(focusedAsState) {
        if (!focusedAsState.value && reminder != uiState.reminder) {
            onReminderInputComplete(reminder)
        }
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val itemModifier = Modifier.padding(horizontal = 20.dp)
        TextAndSwitch(
            title = stringResource(id = R.string.role_guide),
            tip = stringResource(id = R.string.preceding_guide_make_models_play_roles),
            checked = uiState.enableRoleGuide,
            onCheckedChange = onRoleGuideCheckedChange,
            modifier = itemModifier
        )
        TextAndSwitch(
            title = stringResource(id = R.string.reminder_mode),
            tip = stringResource(id = R.string.add_addtional_sentences_before_sending),
            checked = uiState.enableReminderMode,
            onCheckedChange = onReminderModeCheckedChange,
            modifier = itemModifier
        )
        AnimatedVisibility(visible = uiState.enableReminderMode) {
            TTextField(
                text = reminder,
                tip = stringResource(id = R.string.input_on_here),
                onTextChange = { reminder = it },
                interactionSource = interactionSource,
                maxLines = 10,
                modifier = Modifier
                    .then(itemModifier)
                    .fillMaxWidth()
                    .heightIn(min = 80.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    isModified: Boolean,
    isDialogMode: Boolean = false,
    showSave: Boolean = true,
    onClickBack: () -> Unit,
    onClickSave: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                if (!isDialogMode) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = stringResource(id = R.string.back)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.close)
                    )
                }
            }
        },
        actions = {
            AnimatedVisibility(visible = showSave) {
                IconButton(
                    onClick = onClickSave,
                    enabled = isModified,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = stringResource(id = R.string.save),
                    )
                }
            }
        }
    )
//    CenterAlignedTopAppBar(
//        title = { Text(text = title) },
//        modifier = modifier,
//        navigationIcon = {
//            IconButton(onClick = onClickBack) {
//                if (!isDialogMode){
//                    Icon(
//                        imageVector = Icons.Default.ArrowBackIos,
//                        contentDescription = stringResource(id = R.string.back)
//                    )
//                }else{
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = stringResource(id = R.string.close)
//                    )
//                }
//            }
//        },
//        actions = {
//            IconButton(onClick = onClickSave, enabled = isModified) {
//                Icon(
//                    imageVector = Icons.Default.Save,
//                    contentDescription = stringResource(id = R.string.save),
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//    )
}

@Composable
fun HeadAndAvatar(
    modifier: Modifier = Modifier,
    avatar: Uri?,
    onClickAvatar: () -> Unit
) {
    val context = LocalContext.current
    val background by produceState<Bitmap?>(initialValue = null, avatar) {
        value = Utils.blur(context, avatar)
    }
    Box(modifier = modifier) {
        AsyncImage(
            model = background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.TopCenter),
        )
        Row(modifier = Modifier.align(Alignment.BottomStart)) {
            //圆形头像
            AvatarImage(
                model = avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClickAvatar)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
            //更换头像提示
            Text(
                text = stringResource(id = R.string.click_to_change_avatar),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                modifier = Modifier
                    .offset(x = (-16).dp)
                    .align(Alignment.Bottom)
            )
        }
    }
}

@Composable
fun TextAndInput(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    content: String,
    maxLines: Int = 1,
    onTextChange: (String) -> Unit
) {
    val basicHeight = 30.dp
    val minHeight = if (maxLines == 1) basicHeight else basicHeight * maxLines / 2
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 10.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        TTextField(
            text = content,
            onTextChange = onTextChange,
            tip = description,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
            maxLines = maxLines
        )
    }
}


@Composable
private fun UniformDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier.fillMaxWidth(0.7f),
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

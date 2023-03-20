package com.ando.tastechatgpt.ui.screen

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.ando.tastechatgpt.ProfileScreenDestination
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.ext.breathingLight
import com.ando.tastechatgpt.ext.formatByNow
import com.ando.tastechatgpt.ext.toAnnotatedString
import com.ando.tastechatgpt.ext.withCondition
import com.ando.tastechatgpt.model.OpenAIGPT3d5Model
import com.ando.tastechatgpt.ui.component.*
import com.ando.tastechatgpt.ui.screen.state.ChatBottomBarUiState
import com.ando.tastechatgpt.ui.screen.state.ChatEntryUiState
import com.ando.tastechatgpt.ui.screen.state.ChatScreenUiState
import com.ando.tastechatgpt.ui.screen.state.ChatViewModel
import com.ando.tastechatgpt.ui.theme.MessageBubbleShape
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

//TODO: 耳语/提醒模式；正负样本模式/黑白名单

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: ChatViewModel = hiltViewModel(),
    drawerState: DrawerState,
    navigationAction: (String) -> Unit
) {
    var dialogForOpVisible by remember { mutableStateOf(false) }
    var dialogForInputVisible by rememberSaveable { mutableStateOf(false) }
    var longPressedMessageUiState by remember {
        mutableStateOf<ChatEntryUiState?>(null)
    }
    val hapticFeedback = LocalHapticFeedback.current
    val screenUiState = vm.screenUiState
    val currentModelState = screenUiState.currentModel.collectAsState(initial = "")
    val currentModel = currentModelState.value ?: OpenAIGPT3d5Model.modelName
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val lazyPagingItems = screenUiState.flowPagingData.collectAsLazyPagingItems()
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val uiMessage = screenUiState.message
    LaunchedEffect(uiMessage) {
        if (uiMessage.isBlank()) return@LaunchedEffect
        SnackbarUI.showMessage(uiMessage)
    }

    val focusManager = LocalFocusManager.current
    val isFocusedAsState = interactionSource.collectIsFocusedAsState()

    ScreenContent(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures {
                if (isFocusedAsState.value) {
                    focusManager.clearFocus()
                }
            }
        },
        screenUiState = vm.screenUiState,
        lazyPagingItems = lazyPagingItems,
        currentModel = currentModel,
        bottomBarUiState = vm.bottomBarUiState,
        interactionSource = interactionSource,
        onChangingText = vm::updateTextFieldValue,
        sendMessageRequest = vm::sendMessage,
        onClickMenu = {
            scope.launch {
                drawerState.open()
            }
        },
        onUpdateCurrentModel = vm::updateCurrentModel,
        onLongClick = {
            longPressedMessageUiState = it
            dialogForOpVisible = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        onClickAvatar = {
            navigationAction(ProfileScreenDestination.routeWithArg(it))
        }
    )

    if (dialogForOpVisible) {
        DialogForOperationItem(
            onDismissRequest = { dialogForOpVisible = false },
            enableResend = MessageStatus.Failed == longPressedMessageUiState!!.status,
            onClickResend = {
                vm.resendMessage(
                    modelName = currentModel,
                    msgId = longPressedMessageUiState!!.id,
                    previousMsgTime = lazyPagingItems.peek(0)?.timestamp,
                    msg = longPressedMessageUiState!!.text
                )
                dialogForOpVisible = false
            },
            onClickCopy = {
                clipboardManager.setText(longPressedMessageUiState!!.text.toAnnotatedString())
                dialogForOpVisible = false
            },
            onClickDelete = {
                vm.deleteMessage(longPressedMessageUiState!!.id)
                dialogForOpVisible = false
            },
            onClickEdit = {
                dialogForInputVisible = true
                dialogForOpVisible = false
            }
        )
    }

    if (dialogForInputVisible) {
        DialogForStringInput(
            onCancel = { dialogForInputVisible = false },
            onConfirm = {
                vm.updateMessageContent(longPressedMessageUiState!!.id, it)
                dialogForInputVisible = false
            },
            initText = longPressedMessageUiState!!.text
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    screenUiState: ChatScreenUiState,
    bottomBarUiState: ChatBottomBarUiState,
    lazyPagingItems: LazyPagingItems<ChatEntryUiState>,
    currentModel: String,
    interactionSource: MutableInteractionSource,
    onClickMenu: () -> Unit,
    onChangingText: (String) -> Unit,
    sendMessageRequest: (modelName: String, msg: String, previousMsgTime: LocalDateTime?) -> Unit,
    onUpdateCurrentModel: (String) -> Unit,
    onLongClick: (ChatEntryUiState) -> Unit,
    onClickAvatar: (Int) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                title = screenUiState.title,
                onClickMenu = onClickMenu,
                availableList = screenUiState.availableModels,
                selected = currentModel,
                onSelect = onUpdateCurrentModel
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.heightIn(min = 70.dp),
                shadowElevation = 1.dp
            ) {
                BottomBar(
                    text = bottomBarUiState.textValue,
                    onSendingRequest = { content ->
                        sendMessageRequest(
                            currentModel,
                            content,
                            if (lazyPagingItems.itemCount > 0) lazyPagingItems.peek(0)?.timestamp else null
                        )
                    },
                    onChangingText = onChangingText,
                    interactionSource = interactionSource
                )
            }

        }) { paddingValue ->
        ChatArea(
            modifier = Modifier
                .padding(paddingValue)
                .fillMaxSize(),
            pagingItems = lazyPagingItems,
            myId = screenUiState.myId,
            onLongClick = onLongClick,
            onClickAvatar = onClickAvatar
        )
    }


}


@Composable
private fun DialogForOperationItem(
    dialogVisible: Boolean = true,
    onDismissRequest: () -> Unit,
    enableResend: Boolean,
    onClickResend: () -> Unit,
    onClickCopy: () -> Unit,
    onClickDelete: () -> Unit,
    onClickEdit: () -> Unit
) {
    TDialog(dialogVisible = dialogVisible, onDismissRequest = onDismissRequest) {
        if (enableResend) {
            ClickableIconText(
                text = stringResource(id = R.string.resend),
                icon = Icons.Default.Refresh,
                onClick = onClickResend
            )
        }
        ClickableIconText(
            text = stringResource(id = R.string.edit),
            icon = Icons.Default.Edit,
            onClick = onClickEdit
        )
        ClickableIconText(
            text = stringResource(id = R.string.copy),
            icon = Icons.Default.ContentCopy,
            onClick = onClickCopy
        )
        ClickableIconText(
            text = stringResource(id = R.string.delete),
            icon = Icons.Default.Delete,
            contentColor = MaterialTheme.colorScheme.error,
            onClick = onClickDelete
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClickableIconText(
    text: String,
    icon: ImageVector,
    contentColor: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    ListItem(headlineText = {
        Text(text = text, color = contentColor)
    }, leadingContent = {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
    }, modifier = Modifier.clickable(onClick = onClick)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    availableList: List<String>,
    selected: String,
    onClickMenu: () -> Unit,
    onSelect: (String) -> Unit
) {
    var dropdownMenuState by remember {
        mutableStateOf(false)
    }
    CenterAlignedTopAppBar(title = {
        Column {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Box(
                modifier = Modifier.wrapContentSize(Alignment.TopCenter)
            ) {
                Row(modifier = Modifier
                    .clickable { dropdownMenuState = true }
                    .padding(horizontal = 25.dp, vertical = 0.dp)
                    .wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically) {
                    var textHeightDP by remember {
                        mutableStateOf(0.dp)
                    }
                    val density = LocalDensity.current
                    Text(text = selected,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.onSizeChanged {
                            with(density) { textHeightDP = it.height.toDp() }
                        })
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(textHeightDP)
                    )
                }

                DropdownMenu(
                    expanded = dropdownMenuState,
                    onDismissRequest = { dropdownMenuState = false },
                    modifier = Modifier.background(color = Color.Transparent)
                ) {
                    availableList.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            onClick = { onSelect(item);dropdownMenuState = false },
                        )
                    }
                }
            }
        }
    }, navigationIcon = {
        IconButton(onClick = onClickMenu) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = null)
        }
    }, actions = {})
}


/**
 * @param modifier: 修饰符
 * @param textState: 可变状态文本
 * @param onSendingRequest
 * @param interactionSource
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    text: String,
    onSendingRequest: (String) -> Unit,
    onChangingText: (String) -> Unit,
    interactionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    }
) {
    //TODO: 1、点击输入框时关闭面板；2、点击外部时关闭面板
    Row(modifier = modifier.padding(10.dp)) {
        //输入框
        TTextField(
            text = text,
            onTextChange = onChangingText,
            maxLines = 6,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            tip = stringResource(id = R.string.talk_something),
            interactionSource = interactionSource,
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        //发送按钮
        Button(
            onClick = { onSendingRequest(text) },
            modifier = Modifier
                .width(80.dp)
                .height(40.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = stringResource(id = R.string.send), modifier = Modifier
            )
        }
    }
}

@Composable
fun ChatArea(
    modifier: Modifier = Modifier,
    myId: Int,
    pagingItems: LazyPagingItems<ChatEntryUiState>,
    onLongClick: (uiState: ChatEntryUiState) -> Unit,
    onClickAvatar: (uid: Int) -> Unit
) {
    val lazyColumnState = rememberLazyListState()

    //查看最新消息时
    val isLookAtLatest by remember {
        derivedStateOf { lazyColumnState.firstVisibleItemIndex <= 1 }
    }
    LaunchedEffect(pagingItems.loadState.append) {
        Log.i(TAG, "ChatArea: ${pagingItems.loadState.append}")
        //分页加载状态
        when (pagingItems.loadState.append) {
            LoadState.Loading -> {}
            is LoadState.Error -> {}
            else -> {   //没有加载时
                //当查看最新消息时自动滚动最新消息
                //如果页面停留在最新的前两项则自动滚动到最新的一项
                if (isLookAtLatest) {
                    lazyColumnState.animateScrollToItem(0)
                }
            }
        }
    }
    //上下反转的惰性列表
    LazyColumn(
        state = lazyColumnState,
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        modifier = modifier
    ) {
        val itemModifier = Modifier.fillMaxWidth()
        items(items = pagingItems, key = { value: ChatEntryUiState -> value.id }) { item ->
            if (item != null) {
                Message(
                    chatEntryUiState = item,
                    isMe = item.uid == myId,
                    modifier = itemModifier,
                    onLongClick = { onLongClick(item) },
                    onClickAvatar = { onClickAvatar(item.uid) }
                )
            } else {
                //占位符
            }
        }
    }
}

@Composable
fun Message(
    modifier: Modifier = Modifier,
    chatEntryUiState: ChatEntryUiState,
    isMe: Boolean,
    onLongClick: () -> Unit,
    onClickAvatar: () -> Unit
) {
    val annotatedString = chatEntryUiState.text.toAnnotatedString()
    SimpleMessage(
        modifier = modifier,
        avatar = chatEntryUiState.avatar,
        content = annotatedString,
        status = chatEntryUiState.status,
        time = chatEntryUiState.timestamp,
        showTime = Duration.ofSeconds(chatEntryUiState.secondDiff) > showTimeInterval,
        isMe = isMe,
        onLongClick = onLongClick,
        onClickAvatar = onClickAvatar
    )
}


@Composable
fun SimpleMessage(
    modifier: Modifier = Modifier,
    avatar: Any?,
    content: AnnotatedString,
    status: MessageStatus,
    time: LocalDateTime,
    showTime: Boolean = false,
    isMe: Boolean,
    onLongClick: () -> Unit,
    onClickAvatar: () -> Unit
) {
    //颜色
    val colorScheme = MaterialTheme.colorScheme
    //头像大小
    val avatarSize = 40.dp
    //状态图标大小
    val statusIconSize = 22.dp
    //根据isMe调整对齐。手动调整布局防止子组件发生意料之外的情况
    val alignment = if (isMe) Alignment.End else Alignment.Start
    //头像外框颜色
    var borderColor = colorScheme.secondary
    //聊天气泡颜色
    var bubbleColor = colorScheme.surfaceVariant
    //聊天文本颜色
    var textColor = colorScheme.onSurfaceVariant
    if (isMe) {
        borderColor = colorScheme.primary
        bubbleColor = colorScheme.primary
        textColor = colorScheme.onPrimary
    }
    if (status == MessageStatus.Failed){
        bubbleColor = colorScheme.error
    }
    Column(horizontalAlignment = alignment, modifier = modifier) {
        //显示时间
        if (showTime) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text(
                    text = time.formatByNow(LocalContext.current),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Center),
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
            reverseLayout = isMe
        ) {
            item {
                //头像
                AvatarImage(
                    model = avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .border(
                            width = 1.6.dp, color = borderColor, shape = CircleShape
                        )
                        .border(
                            width = 3.2.dp,
                            color = colorScheme.surface,
                            shape = CircleShape
                        )
                        .background(color = Color.White)
                        .clickable(onClick = onClickAvatar)
                )
                //聊天气泡内容
                Spacer(modifier = Modifier.width(10.dp))
                BubbleText(
                    text = content,
                    containerColor = bubbleColor,
                    textColor = textColor,
                    modifier = Modifier
//                        .combinedClickable(onLongClick = onLongClick, onClick = onClick)
                        .withCondition(
                            status == MessageStatus.Sending,
                            Modifier.breathingLight(rememberBreathingLightState())
                        )
                        .sizeIn(
                            minHeight = avatarSize, minWidth = 0.dp, maxWidth = 240.dp
                        ),
                    forwardLeft = !isMe,
                    onLongClick = onLongClick,
                    onClick = onClickAvatar
                )
//                //发送状态
//                Spacer(modifier = Modifier.width(5.dp))
//                Box(modifier = Modifier.size(statusIconSize)) {
//                    status.ui()
//                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun BubbleText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    textColor: Color = LocalContentColor.current,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    forwardLeft: Boolean = true,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val shape = if (forwardLeft) {
        RoundedCornerShape(5.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 5.dp, 20.dp, 20.dp)
    }
    val layoutResult = remember {
        mutableStateOf<TextLayoutResult?>(null)
    }
    val onClickText = { offset: Int -> /*TODO: 点击文字特殊tag做出响应*/ }
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Surface(color = containerColor,
        shape = shape,
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .wrapContentSize()
            .indication(interactionSource, rememberRipple())
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        Log.i(TAG, "BubbleText: onLongPress: offset=$it")
                        onLongClick()
                    },
                    onTap = { pos ->
                        layoutResult.value?.let { layout ->
                            Log.i(
                                TAG,
                                "BubbleText: onTap: pos = $pos, layout = $layout, offsetForPosition = ${
                                    layout.getOffsetForPosition(pos)
                                }"
                            )
                            onClick()
                            onClickText(layout.getOffsetForPosition(pos))
                        }
                    },
                    onPress = { offset ->
                        val pressInteraction = PressInteraction.Press(offset)
                        interactionSource.emit(pressInteraction)
                        val release = tryAwaitRelease()
                        if (release)
                            interactionSource.emit(PressInteraction.Release(pressInteraction))
                        else
                            interactionSource.emit(PressInteraction.Cancel(pressInteraction))
                    }
                )
            }
    ) {
        Text(text = text,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize(),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = textColor, textAlign = TextAlign.Start
            ),
            onTextLayout = {
                layoutResult.value = it
            })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
private fun PreviewTopBar() {
    TopBar(title = "标题",
        availableList = listOf("选项1", "选项2"),
        selected = "选项1",
        onClickMenu = { /*TODO*/ },
        onSelect = {})
}

@Preview(showBackground = true)
@Composable
private fun PreviewChatEntry() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()
    ) {
        SimpleMessage(avatar = R.drawable.avatar_user0,
            content = " ".toAnnotatedString(),
            status = MessageStatus.Failed,
            time = LocalDateTime.now(),
            isMe = false,
            onLongClick = {},
            onClickAvatar = {})
    }
}

@Preview
@Composable
fun PreviewBottomBar() {
    val text by remember {
        mutableStateOf("")
    }
    BottomBar(text = text, onSendingRequest = {}, onChangingText = {})
}


private val messageBubbleShape = MessageBubbleShape(
    cornerSize = 15.dp, triangle = MessageBubbleShape.Triangle(
        base = 10.dp, height = 6.dp, fromTop = 17.dp
    )
)
private val showTimeInterval = Duration.ofMinutes(15)
private const val TAG = "ChatScreen"
private val myBackgroundColor = Color(0x00b4d8)
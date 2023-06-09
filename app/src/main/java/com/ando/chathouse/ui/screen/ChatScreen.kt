@file:OptIn(ExperimentalFoundationApi::class)

package com.ando.chathouse.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.ando.chathouse.ProfileScreenDestination
import com.ando.chathouse.R
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.Container
import com.ando.chathouse.domain.pojo.getValue
import com.ando.chathouse.domain.pojo.setValue
import com.ando.chathouse.ext.*
import com.ando.chathouse.ui.component.*
import com.ando.chathouse.ui.component.dragfetch.DragFetchState
import com.ando.chathouse.ui.component.exclusive.ChatScreenExtendedBottomBar
import com.ando.chathouse.ui.component.exclusive.ChatScreenExtendedTopBar
import com.ando.chathouse.ui.screen.state.ChatMessageUiState
import com.ando.chathouse.ui.screen.state.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    drawerState: DrawerState,
    navigationAction: (String) -> Unit
) {
    var dialogForOpVisible by remember { mutableStateOf(false) }
    var dialogForInputVisible by rememberSaveable { mutableStateOf(false) }
    var dialogForWarning by remember { mutableStateOf(false) }
    var longPressedMessageUiState: ChatMessageUiState? by remember { mutableStateOf(null) }
    val hapticFeedback = LocalHapticFeedback.current
    val screenUiState = viewModel.screenUiState
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val lazyPagingItems = screenUiState.lazyPagingDataItems()
    val uiMessage = screenUiState.message
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiMessage) {
        if (uiMessage.isBlank()) return@LaunchedEffect
        SnackbarUI.showMessage(uiMessage)
        viewModel.resetMessage()
    }
    DisposableEffect(Unit){
        onDispose {
            viewModel.resetMessage()
        }
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures {
                focusManager.clearFocus()
            }
        },
        topBar = {
            ChatScreenExtendedTopBar(
                uiState = screenUiState.topBarUiState,
                onClickMenu = { scope.launch { drawerState.open() } },
                onClickEditButton = viewModel::switchEditModeState,
                onSelectModel = viewModel::updateCurrentModel,
                onSelectStrategy = viewModel::updateStrategy,
                onMultiSelectModeChange = viewModel::switchMultiSelectModeState,
                onClickClearConversation = { dialogForWarning = true },
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 2.dp
            ) {
                ChatScreenExtendedBottomBar(
                    uiState = screenUiState.bottomBarUiState,
                    onSend = { fromMe, content ->
                        viewModel.sendMessage(
                            msg = content,
                            previousMsgTime = if (lazyPagingItems.itemCount > 0) lazyPagingItems.peek(
                                0
                            )?.timestamp else null,
                            fromMe = fromMe
                        )
                    },
                    onTextChange = viewModel::updateInputText,
                    onClickCarry = viewModel::selectedToCarry,
                    onClickExclude = viewModel::selectedToExclude,
                    onClickDelete = viewModel::selectedTODelete,
                    onGoOn = viewModel::goOn
                )
            }
        }
    ) { paddingValue ->
        SwipeLoading(
            isFetching = screenUiState.goOnState,
            onFetch = viewModel::goOn,
            fetchIndicator = {
                FetchIndicator(it)
            },
            liftThreshold = (-60).dp,
            modifier = Modifier
                .padding(paddingValue)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            ChatArea(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                pagingItems = lazyPagingItems,
                myId = screenUiState.myId,
                onLongClick = {
                    longPressedMessageUiState = it
                    dialogForOpVisible = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onClickAvatar = {
                    navigationAction(ProfileScreenDestination.routeWithArg(it))
                },
                onClickBubble = {},
                onChecked = viewModel::checkedMessageId
            )
        }
    }




    if (dialogForOpVisible) {
        DialogForOperationItem(
            onDismissRequest = { dialogForOpVisible = false },
//            enableResend = longPressedMessageUiState!!.status == MessageStatus.Failed,
            enableResend = longPressedMessageUiState?.uid == screenUiState.myId,
            onClickResend = {
                viewModel.resendMessage(
                    msgId = longPressedMessageUiState!!.id
                )
                dialogForOpVisible = false
            },
            onClickCopy = {
                clipboardManager.setText(longPressedMessageUiState!!.text.toAnnotatedString())
                dialogForOpVisible = false
            },
            onClickDelete = {
                viewModel.deleteMessage(longPressedMessageUiState!!.id)
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
                viewModel.updateMessageContent(longPressedMessageUiState!!.id, it)
                dialogForInputVisible = false
            },
            initText = longPressedMessageUiState!!.text,
            modifier = Modifier
                .wrapContentSize()
        )
    }

    if (dialogForWarning) {
        SimpleAlertDialog(
            dialogVisible = dialogForWarning,
            onCancel = { dialogForWarning = false },
            onConfirm = {
                dialogForWarning = false
                viewModel.clearConversation()
                //关闭编辑模式
                viewModel.switchEditModeState(false)
            },
            title = { Text(text = stringResource(id = R.string.confirm_delete)) }
        ) {
            Text(text = stringResource(id = R.string.warning_clear_conversation))
        }
    }


}

@Composable
private fun FetchIndicator(dragFetchState: DragFetchState) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.go_on),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            letterSpacing = 10.sp
        )
        if (dragFetchState.fetching){
            CircularProgressIndicator(strokeWidth = 8.dp, modifier = Modifier.scale(0.7f))
        }
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
    val itemModifier = Modifier.width(300.dp)
    TDialog(dialogVisible = dialogVisible, onDismissRequest = onDismissRequest) {
        if (enableResend) {
            ClickableIconTextListItem(
                text = stringResource(id = R.string.resend),
                icon = Icons.Default.Refresh,
                onClick = onClickResend,
                modifier = itemModifier
            )
        }
        ClickableIconTextListItem(
            text = stringResource(id = R.string.edit),
            icon = Icons.Default.Edit,
            onClick = onClickEdit,
            modifier = itemModifier
        )
        ClickableIconTextListItem(
            text = stringResource(id = R.string.copy),
            icon = Icons.Default.ContentCopy,
            onClick = onClickCopy,
            modifier = itemModifier
        )
        ClickableIconTextListItem(
            text = stringResource(id = R.string.delete),
            icon = Icons.Default.Delete,
            contentColor = MaterialTheme.colorScheme.error,
            onClick = onClickDelete,
            modifier = itemModifier
        )
    }
}


@Composable
fun ChatArea(
    modifier: Modifier = Modifier,
    myId: Int,
    pagingItems: LazyPagingItems<ChatMessageUiState>,
    onLongClick: (uiState: ChatMessageUiState) -> Unit,
    onClickAvatar: (uid: Int) -> Unit,
    onClickBubble: (msgId: Int) -> Unit,
    onChecked: (Int) -> Unit,
) {
    val lazyColumnState = rememberLazyListState()
    var lastItemCount by remember{
        Container(0)
    }
    //TODO: 导航到最新消息的浮动按钮
    LaunchedEffect(pagingItems.itemCount) {
        //新增记录则滚动到最新消息
        if (lastItemCount<pagingItems.itemCount){
            //检查是否已经装配好，是否已显示最新
            if (lazyColumnState.firstVisibleItemIndex!=0) {
                lazyColumnState.animateScrollToItem(0)
            }
        }
        //更新
        if (lastItemCount != pagingItems.itemCount){
            lastItemCount = pagingItems.itemCount
        }
    }
    //上下反转的惰性列表
    LazyColumn(
        state = lazyColumnState,
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(10.dp),
        modifier = modifier
    ) {
        val itemModifier = Modifier.fillMaxWidth()
        items(
            items = pagingItems,
            key = { it.id }
        ) { item ->
            if (item != null) {
                val rememberItem = remember {
                    mutableStateOf(item)
                }
                LaunchedKeyEffect(item) {
                    rememberItem.value = item
                }
                val onLongClickLocal = {
                    onLongClick(rememberItem.value)
                }
                SimpleMessage(
                    messageUiState = item,
                    isMe = item.uid == myId,
                    showTime = item.secondDiff >= DISPLAY_TIME_THRESHOLD_SECOND,
                    modifier = itemModifier,
                    onLongClick = onLongClickLocal,
                    onClickAvatar = { onClickAvatar(item.uid) },
                    onClickBubble = { onClickBubble(item.id) },
                    onChecked = onChecked
                )
            } else {
                //占位符
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }
        }
    }
}


@Composable
fun SimpleMessage(
    modifier: Modifier = Modifier,
    messageUiState: ChatMessageUiState,
    showTime: Boolean = false,
    isMe: Boolean,
    onLongClick: () -> Unit,
    onClickAvatar: () -> Unit,
    onClickBubble: () -> Unit,
    onChecked: (Int)->Unit,
) {
    //颜色
    val colorScheme = MaterialTheme.colorScheme
    //头像大小
    val avatarSize = 40.dp
    //根据isMe调整对齐。手动调整布局防止子组件发生意料之外的情况
//    val alignment = if (isMe) Alignment.End else Alignment.Start
    //头像外框颜色
    var borderColor = colorScheme.secondary
    if (isMe) {
        borderColor = colorScheme.primary
    }
    Column(modifier = modifier) {
        //显示时间
        if (showTime) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text(
                    text = messageUiState.timestamp.formatByNow(LocalContext.current),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Center),
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        ReversibleRow(
            reverseLayout = isMe,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            //头像
            AvatarImage(
                model = messageUiState.avatar,
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
            Spacer(modifier = Modifier.width(10.dp))
            //聊天气泡内容
            BoxWithConstraints {
                ExtendedBubbleText(
                    uiState = messageUiState.bubbleTextUiState,
                    onLongClick = onLongClick,
                    onClick = onClickBubble,
                    modifier = Modifier
                        .withCondition(
                            messageUiState.status == MessageStatus.Sending,
                            Modifier.breathingLight(rememberBreathingLightState())
                        )
                        .heightIn(min = avatarSize)
                        .widthIn(max = this.maxWidth - avatarSize - 10.dp),
                    onChecked = onChecked
                )
            }

            if (messageUiState.status == MessageStatus.Failed) {
                Text(
                    text = stringResource(id = R.string.send_failure),
                    color = colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
            if (messageUiState.status == MessageStatus.Interrupt) {
                Text(
                    text = stringResource(id = R.string.receive_interrupted),
                    color = colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }

        }
    }
}


private const val TAG = "ChatScreen"

//相邻的两条消息时间差超过5分钟则显示时间
private const val DISPLAY_TIME_THRESHOLD_SECOND = 5 * 60
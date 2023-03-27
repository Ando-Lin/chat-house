@file:OptIn(ExperimentalMaterial3Api::class)

package com.ando.chathouse.ui.screen

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.ando.chathouse.ChatScreenTabDestination
import com.ando.chathouse.ProfileScreenDestination
import com.ando.chathouse.R
import com.ando.chathouse.constant.MY_UID
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.ext.withMutableInteractionSource
import com.ando.chathouse.ui.component.*
import com.ando.chathouse.ui.screen.state.RoleListScreenViewModel
import com.ando.chathouse.ui.theme.ChatHouseTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun RoleListScreen(
    drawerState: DrawerState,
    viewModel: RoleListScreenViewModel = hiltViewModel(),
    navigationAction: (routeWithArg: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var popupVisible by remember {
        mutableStateOf(false)
    }
    var dialogVisible by remember {
        mutableStateOf(false)
    }
    var offset by remember {
        mutableStateOf(IntOffset.Zero)
    }
    var uid by remember {
        mutableStateOf(0)
    }
    val uiState = viewModel.screenUiState
    val lazyPagingItems = uiState.pagingDataFlow.collectAsLazyPagingItems()
    val message = viewModel.screenUiState.message
    val hapticFeedback = LocalHapticFeedback.current
    val refreshState = !lazyPagingItems.loadState.append.endOfPaginationReached
    Log.i(TAG, "RoleListScreen: loadState = ${lazyPagingItems.loadState}")

    LaunchedEffect(message) {
        if (message.isBlank()) return@LaunchedEffect
        SnackbarUI.showMessage(message = message)
        viewModel.resetMessage()
    }


    SimpleAlertDialog(
        dialogVisible = dialogVisible,
        onCancel = { dialogVisible = false },
        onConfirm = { viewModel.delete(uid);dialogVisible = false }
    ) {
        Text(text = stringResource(R.string.ask_for_delete))
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.role_list),
                onClickMenu = { scope.launch { drawerState.open() } },
                onCLickAdd = { navigationAction(ProfileScreenDestination.routeWithArg(0)) }
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(refreshState),
            onRefresh = { lazyPagingItems.refresh() }) {
            ScreenContent(
                lazyPagingItems = lazyPagingItems,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                onClickRoleItem = {
                    if (it != MY_UID) {
                        navigationAction(
                            ChatScreenTabDestination.routeWithArg(it)
                        )
                    }
                },
                onClickAvatar = {
                    navigationAction(
                        ProfileScreenDestination.routeWithArg(it)
                    )
                },
                onLongPressRoleItem = { o, u ->
                    offset = IntOffset(o.x.toInt(), o.y.toInt())
                    uid = u
                    popupVisible = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )
        }
    }

    TDialog(dialogVisible = popupVisible, onDismissRequest = { popupVisible = false }) {
        val modifier = Modifier
            .padding(vertical = 7.dp)
            .width(220.dp)
            .wrapContentHeight()

        ClickableIconTextListItem(
            text = stringResource(id = R.string.edit),
            icon = Icons.Default.Edit,
            onClick = {
                popupVisible = false
                navigationAction(ProfileScreenDestination.routeWithArg(uid))
            },
            modifier = modifier
        )

        if (uid == viewModel.screenUiState.myId) return@TDialog

        ClickableIconTextListItem(
            text = stringResource(id = R.string.delete),
            icon = Icons.Default.Delete,
            onClick = {
                dialogVisible = true
                popupVisible = false
            },
            contentColor = MaterialTheme.colorScheme.error,
            modifier = modifier
        )
    }

}

@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    lazyPagingItems: LazyPagingItems<User>,
    onClickAvatar: (uid: Int) -> Unit,
    onClickRoleItem: (uid: Int) -> Unit,
    onLongPressRoleItem: (offset: Offset, uid: Int) -> Unit,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RectangleShape),
        contentPadding = PaddingValues(vertical = 5.dp)
    ) {
        items(items = lazyPagingItems, key = { it.id }) { user ->
            val interactionSource = remember { MutableInteractionSource() }
            if (user != null) {
                ListItem(
                    headlineText = {
                        Text(text = user.name, maxLines = 1)
                    },
                    leadingContent = {
                        AvatarImage(
                            model = user.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .aspectRatio(1f)
                                .clickable { onClickAvatar(user.id) }
                        )
                    },
                    supportingText = {
                        Text(
                            text = user.description,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .indication(
                            interactionSource = interactionSource,
                            indication = rememberRipple()
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onLongPressRoleItem(it, user.id) },
                                onTap = { onClickRoleItem(user.id) },
                                onPress = {
                                    withMutableInteractionSource(it, interactionSource)
                                }
                            )
                        },
                )
            }
        }
    }
}


@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    onClickMenu: () -> Unit,
    onCLickAdd: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickMenu) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onCLickAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add)
                )
            }
        }
    )
}

private val pagingDataFlow = flowOf(
    PagingData.from(
        listOf(
            User(id = 1, name = "aba", avatar = null, description = "描述"),
            User(
                id = 2,
                name = "派蒙",
                avatar = null,
                description = "这是描述为白兰民描述这些天来遇到的凶险旅程！顺便摸摸老头们鼓鼓的米袋"
            ),
        )
    )
)

@Preview
@Composable
fun RoleListScreenPrev() {
    ChatHouseTheme {
        val lazyPagingItems = pagingDataFlow.collectAsLazyPagingItems()
        Surface {
            ScreenContent(
                lazyPagingItems = lazyPagingItems,
                modifier = Modifier.fillMaxSize(),
                onClickAvatar = {
                    Log.i(TAG, "RoleListScreenPrev: onClickAvatar: $it")
                },
                onClickRoleItem = {
                    Log.i(TAG, "RoleListScreenPrev: onClickRoleItem: $it")
                },
                onLongPressRoleItem = { offset, uid -> },
            )

        }
    }
}

private const val TAG = "RoleListScreen"
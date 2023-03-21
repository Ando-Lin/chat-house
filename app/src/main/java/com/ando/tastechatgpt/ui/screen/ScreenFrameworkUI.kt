@file:OptIn(ExperimentalFoundationApi::class)

package com.ando.tastechatgpt.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.ando.tastechatgpt.ChatScreenTabDestination
import com.ando.tastechatgpt.MainScreenDestination
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.VisualTabDestination
import com.ando.tastechatgpt.ui.component.AvatarImage
import com.ando.tastechatgpt.ui.component.TDrawer
import com.ando.tastechatgpt.ui.screen.state.MainScreenViewModel
import com.ando.tastechatgpt.ui.screen.state.RecentChat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenFrameworkUI(
    drawerState: DrawerState,
    visualTabDestinationList: List<VisualTabDestination>,
    enableDrawer: Boolean,
    currentPage: Int,
    navigationRequest: (String) -> Unit,
    scrollToPageRequest: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    if (drawerState.isOpen) {
        BackHandler {
            scope.launch { drawerState.close() }
        }
    }

    //抽屉
    TDrawer(
        modifier = Modifier,
        enable = enableDrawer,
        navigateAction = { navigationRequest(it) },
        drawerState = drawerState,
        drawerContent = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationList(
                visualTabDestinationList = visualTabDestinationList,
                currentPage = currentPage,
                scrollToPageRequest = {
                    scrollToPageRequest(it)
                },
            )
        },
        content = content
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationList(
    modifier: Modifier = Modifier,
    visualTabDestinationList: List<VisualTabDestination>,
    currentPage: Int,
    scrollToPageRequest: (Int) -> Unit
) {
    visualTabDestinationList.forEachIndexed { index, item ->
        val selected = currentPage == index
        NavigationDrawerItem(
            label = {
                Text(
                    text = stringResource(id = item.labelResId),
                )
            },
            selected = selected,
            onClick = {
                scrollToPageRequest(index)
            },
            icon = {
                val iconResId = if (selected) {
                    item.selectedIconResId
                } else {
                    item.unselectedIconResId
                }
                Icon(
                    imageVector = ImageVector.vectorResource(id = iconResId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentChatList(
    lazyPagingItems: LazyPagingItems<RecentChat>,
    currentChatId: Int,
    navigateRequest: (String) -> Unit
) {
    LazyColumn() {
        val modifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .height(40.dp)
        items(items = lazyPagingItems, key = RecentChat::chatId) { item ->
            if (item != null) {
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selected = currentChatId == item.chatId,
                    onClick = { navigateRequest(ChatScreenTabDestination.routeWithArg(item.chatId)) },
                    icon = {
                        AvatarImage(
                            model = item.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                        )
                    },
                    modifier = modifier
                )
                Log.i(
                    TAG,
                    "MainScreenFramework: currentChatId=$currentChatId    item.chatId=${item.chatId}"
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    visualTabDestinationList: List<VisualTabDestination>,
    currentRoute: String,
    navigationRequest: (String) -> Unit
) {
    NavigationBar {
        visualTabDestinationList.forEach { vd: VisualTabDestination ->
            val selected = currentRoute == vd.route
            NavigationBarItem(
                selected = selected,
                onClick = { navigationRequest(vd.route) },
                icon = {
                    val iconResId = if (selected) {
                        vd.selectedIconResId
                    } else {
                        vd.unselectedIconResId
                    }
                    Icon(
                        imageVector = ImageVector.vectorResource(id = iconResId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(text = stringResource(id = vd.labelResId)) }
            )
        }
    }

}

private const val TAG = "ScreenFrameworkUI"
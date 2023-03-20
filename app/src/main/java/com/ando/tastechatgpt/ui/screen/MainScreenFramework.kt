package com.ando.tastechatgpt.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.ando.tastechatgpt.ChatScreenDestination
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.VisualDestination
import com.ando.tastechatgpt.ext.navigateSingleTop
import com.ando.tastechatgpt.ui.component.AvatarImage
import com.ando.tastechatgpt.ui.component.TDrawer
import com.ando.tastechatgpt.ui.screen.state.MainScreenViewModel
import com.ando.tastechatgpt.ui.screen.state.RecentChat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenFramework(
    navHostController: NavHostController,
    drawerState: DrawerState,
    visualDestinationList: List<VisualDestination>,
    startDestination: String,
    viewModel: MainScreenViewModel = hiltViewModel(),
    enableDrawer: Boolean,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    if (drawerState.isOpen) {
        BackHandler {
            scope.launch { drawerState.close() }
        }
    }
    val drawerUiState = viewModel.drawerUiState
    val lazyPagingItems = drawerUiState.pagingDataFlow.collectAsLazyPagingItems()
    val currentChatId by drawerUiState.currentChatId.collectAsState(initial = 0)
    //当前route
    val destination = navHostController.currentDestination
    destination?.displayName
    val currentRoute = navHostController.currentDestination?.route ?: startDestination
    Log.i(TAG, "MainScreenFramework: currentRoute=$currentRoute")
    Log.i(TAG, "MainScreenFramework: currentChatId=$currentChatId")
    //抽屉
    TDrawer(
        modifier = Modifier,
        enable = enableDrawer,
        navigateAction = { navHostController.navigateSingleTop(it) },
        drawerState = drawerState,
        drawerContent = {
            Text(
                text = stringResource(R.string.navigation),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 10.dp).padding(top = 10.dp, bottom = 7.dp)
            )
            NavigationList(
                visualDestinationList = visualDestinationList,
                currentRoute = currentRoute,
                navigationRequest = {
                    scope.launch {
                        navHostController.navigateSingleTop(it)
                        drawerState.close()
                    }
                },
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                //导航栏
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationList(
    modifier: Modifier = Modifier,
    visualDestinationList: List<VisualDestination>,
    currentRoute: String,
    navigationRequest: (String) -> Unit
) {
    visualDestinationList.forEach { item ->
        val selected = currentRoute == item.realRoute
        NavigationDrawerItem(
            label = {
                Text(
                    text = stringResource(id = item.labelResId),
                )
            },
            shape = RectangleShape,
            selected = selected,
            onClick = { navigationRequest(item.route) },
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
            modifier = modifier
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
                    onClick = { navigateRequest(ChatScreenDestination.routeWithArg(item.chatId)) },
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
    visualDestinationList: List<VisualDestination>,
    currentRoute: String,
    navigationRequest: (String) -> Unit
) {
    NavigationBar {
        visualDestinationList.forEach { vd: VisualDestination ->
            val selected = currentRoute == vd.realRoute
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

private const val TAG = "MainScreenFramework"
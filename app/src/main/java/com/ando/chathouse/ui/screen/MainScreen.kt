@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.ando.chathouse.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import com.ando.chathouse.ChatScreenTabDestination
import com.ando.chathouse.MainScreenDestination
import com.ando.chathouse.RoleListScreenTabDestination
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList


object MainScreen {


    val navigationDestinations by
    mutableStateOf(listOf(ChatScreenTabDestination, RoleListScreenTabDestination))
    val destinations = navigationDestinations.map { it.route }.toImmutableList()
    val defaultTabDestination = ChatScreenTabDestination.route
    val defaultPage = destinations.indexOf(defaultTabDestination)


    @Composable
    fun ScreenDrawer(
        drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
        navigationRequest: (String) -> Unit,
        enableDrawer: Boolean = true,
        pagerState: PagerState,
        content: @Composable () -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        ScreenFrameworkUI(
            drawerState = drawerState,
            visualTabDestinationList = { navigationDestinations },
            enableDrawer = enableDrawer,
            navigationRequest = {
                navigationRequest(it)
                scope.launch {
                    drawerState.close()
                }
            },
            scrollToPageRequest = {
                scope.launch {
//                    pagerState.animateScrollToPage(it)
                    navigationRequest(MainScreenDestination.routeWithArg(destinations[it]))
                }
                scope.launch {
                    drawerState.close()
                }
            },
            currentPage = { pagerState.currentPage },
            content = content
        )
    }

    @Composable
    fun ScreenUI(
        key: Any?,
        requestTab: String,
        pagerState: PagerState,
        navigationRequest: (String) -> Unit,
    ) {
        LaunchedEffect(key) {
            pagerState.animateScrollToPage(destinations.indexOf(requestTab))
        }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        ScreenDrawer(
            navigationRequest = navigationRequest,
            pagerState = pagerState,
            drawerState = drawerState
        ) {
            MainScreen(
                tabs = destinations,
                drawerState = drawerState,
                navigationAction = navigationRequest,
                pagerState = pagerState
            )
        }
    }

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    tabs: List<String>,
    drawerState: DrawerState,
    navigationAction: (String) -> Unit,
    pagerState: PagerState
) {
    VerticalPager(
        pageCount = tabs.size,
        state = pagerState,
        beyondBoundsPageCount = 1,
        userScrollEnabled = false
    ) {
        when (tabs[it]) {
            ChatScreenTabDestination.route -> {
                ChatScreen(drawerState = drawerState, navigationAction = navigationAction)
            }
            RoleListScreenTabDestination.route -> {
                RoleListScreen(drawerState = drawerState, navigationAction = navigationAction)
            }
        }
    }
}

private const val TAG = "MainScreen"
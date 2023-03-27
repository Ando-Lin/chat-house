@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.ando.tastechatgpt.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import com.ando.tastechatgpt.ChatScreenTabDestination
import com.ando.tastechatgpt.RoleListScreenTabDestination
import com.ando.tastechatgpt.ui.component.LaunchedKeyEffect
import com.ando.tastechatgpt.ui.screen.MainScreen.drawerState
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList


object MainScreen {

    lateinit var drawerState: DrawerState

    val navigationDestinations =
        listOf(ChatScreenTabDestination, RoleListScreenTabDestination).toMutableStateList()
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
        LaunchedEffect(drawerState) {
            MainScreen.drawerState = drawerState
        }
        val scope = rememberCoroutineScope()
        ScreenFrameworkUI(
            drawerState = drawerState,
            visualTabDestinationList = navigationDestinations,
            enableDrawer = enableDrawer,
            navigationRequest = {
                navigationRequest(it)
                scope.launch {
                    drawerState.close()
                }
            },
            scrollToPageRequest = {
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
                scope.launch {
                    drawerState.close()
                }
            },
            currentPage = pagerState.currentPage,
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
        LaunchedKeyEffect(key){
            pagerState.animateScrollToPage(destinations.indexOf(requestTab))
        }
//        Log.i(TAG, "MainScreen: cuurrentTab=$requestTab tabs=$destinations key=$keyState")
        MainScreen(
            tabs = destinations,
            drawerState = drawerState,
            navigationAction = navigationRequest,
            pagerState = pagerState
        )
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
        beyondBoundsPageCount = 0,
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
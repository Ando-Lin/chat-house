@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.ando.chathouse.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import com.ando.chathouse.ChatScreenTabDestination
import com.ando.chathouse.RoleListScreenTabDestination
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList


object MainScreen {
    val navigationDestinations by
    mutableStateOf(listOf(ChatScreenTabDestination, RoleListScreenTabDestination))
    val destinations = navigationDestinations.map { it.realRoute }.toImmutableList()
    val defaultTabDestination = ChatScreenTabDestination.route
    val defaultPage = destinations.indexOf(defaultTabDestination)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ScreenDrawer(
        drawerState: DrawerState,
        navigationRequest: (String) -> Unit,
        enableDrawer: Boolean = true,
        currentRoute: () -> String,
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
                    navigationRequest(destinations[it])
                }
                scope.launch {
                    drawerState.close()
                }
            },
            currentRoute = currentRoute,
            content = content
        )
    }

    @Composable
    fun MChatScreen(
        navigationRequest: (String) -> Unit,
        drawerState: DrawerState
    ) {
        ScreenDrawer(
            navigationRequest = navigationRequest,
            currentRoute = { ChatScreenTabDestination.realRoute },
            drawerState = drawerState
        ) {
            ChatScreen(
                drawerState = drawerState,
                navigationAction = navigationRequest
            )
        }
    }

    @Composable
    fun MRoleListScreen(
        navigationRequest: (String) -> Unit,
        drawerState: DrawerState
    ) {
        ScreenDrawer(
            navigationRequest = navigationRequest,
            currentRoute = { RoleListScreenTabDestination.realRoute },
            drawerState = drawerState
        ) {
            RoleListScreen(
                drawerState = drawerState,
                navigationAction = navigationRequest
            )
        }
    }

}


private const val TAG = "MainScreen"
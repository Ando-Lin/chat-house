@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.ando.chathouse.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.ando.chathouse.ChatScreenTabDestination
import com.ando.chathouse.RoleListScreenTabDestination
import com.ando.chathouse.SettingScreenDestination
import com.ando.chathouse.ext.navigatePopup
import com.ando.chathouse.ext.navigateSingleTop
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
        navigateToSettingsRequest: () -> Unit,
        navigateToTabRequest: (route:String)->Unit,
        enableDrawer: Boolean = true,
        currentRoute: () -> String,
        content: @Composable () -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        ScreenFrameworkUI(
            drawerState = drawerState,
            visualTabDestinationList = { navigationDestinations },
            enableDrawer = enableDrawer,
            navigateToSettingsRequest = {
                navigateToSettingsRequest()
                scope.launch {
                    drawerState.close()
                }
            },
            navigateToTabRequest = {
                scope.launch {
                    navigateToTabRequest(destinations[it])
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
        navController: NavHostController,
        drawerState: DrawerState
    ) {
        ScreenDrawer(
            navigateToTabRequest = navController::navigatePopup,
            navigateToSettingsRequest = {navController.navigateSingleTop(SettingScreenDestination.route)},
            currentRoute = { ChatScreenTabDestination.realRoute },
            drawerState = drawerState
        ) {
            ChatScreen(
                drawerState = drawerState,
                navigationAction = navController::navigateSingleTop
            )
        }
    }

    @Composable
    fun MRoleListScreen(
        navController: NavHostController,
        drawerState: DrawerState
    ) {
        ScreenDrawer(
            navigateToTabRequest = navController::navigatePopup,
            navigateToSettingsRequest = {navController.navigateSingleTop(SettingScreenDestination.route)},
            currentRoute = { RoleListScreenTabDestination.realRoute },
            drawerState = drawerState
        ) {
            RoleListScreen(
                drawerState = drawerState,
                navigationAction = navController::navigateSingleTop
            )
        }
    }

}


private const val TAG = "MainScreen"
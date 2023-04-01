@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class, ExperimentalAnimationApi::class
)

package com.ando.chathouse

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavHostController
import com.ando.chathouse.constant.PreferencesKey
import com.ando.chathouse.ui.component.SnackbarUI
import com.ando.chathouse.ui.screen.*
import com.ando.chathouse.ui.theme.ChatHouseTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置compose
        setContent {
            val navController = rememberAnimatedNavController()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            Init()
            ChatHouseTheme {
                Scaffold(
                    snackbarHost = { SnackbarUI.ComposeUI() }
                ) { paddingValue ->
                    Surface(
                        modifier = Modifier
                            .padding(paddingValue)
                            .fillMaxSize(),
                    ) {
                        Navigation(
                            navController = navController,
                            drawerState = drawerState
                        )
                    }
                }

            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Navigation(
    navController: NavHostController,
    drawerState: DrawerState
) {
    val animateTime = 400
    AnimatedNavHost(
        navController = navController,
        startDestination = ChatScreenTabDestination.routeWithArg,
    ) {
        composable(
            route = ChatScreenTabDestination.routeWithArg,
            arguments = ChatScreenTabDestination.arguments,
            enterTransition = {
                Log.i(TAG, "Navigation: enter route = ${initialState.destination.route}")
                when(initialState.destination.route){
                    RoleListScreenTabDestination.realRoute ->
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Down,tween(animateTime))
                    else -> null
                }

            },
            exitTransition = {
                when(targetState.destination.route){
                    RoleListScreenTabDestination.realRoute ->
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Up,tween(animateTime))
                    else -> null
                }
            },
        ) { navBackStackEntry ->
            val arguments = navBackStackEntry.arguments
            //更新参数
            val params = arguments?.getString(ChatScreenTabDestination.argName)
            val context = LocalContext.current
            //将dataStore的currentChatId作为屏幕显示唯一可信源
            LaunchedEffect(params) {
                val chatId = params?.toIntOrNull()
                if (chatId != null) {
                    context.profile.edit {
                        it[PreferencesKey.currentChatId] = chatId
                    }
                }
            }
            MainScreen.MChatScreen(
                navController = navController,
                drawerState = drawerState
            )
        }
        composable(
            route = RoleListScreenTabDestination.route,
            enterTransition = {
                when (initialState.destination.route) {
                    ChatScreenTabDestination.realRoute ->
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Up, tween(animateTime))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    ChatScreenTabDestination.realRoute ->
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, tween(animateTime)
                    )
                    else -> null
                }

            },
        ) {
            MainScreen.MRoleListScreen(
                navController = navController,
                drawerState = drawerState
            )
        }
        composable(route = SettingScreenDestination.route, enterTransition = null) {
            SettingScreen(backAction = { navController.popBackStack() })
        }
        composable(
            route = ProfileScreenDestination.routeWithArg,
            arguments = ProfileScreenDestination.arguments,
            enterTransition = null
        ) {
            ProfileScreen(backAction = { navController.popBackStack() })
        }
    }
}


@Composable
private fun Init() {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionMap ->
        val allGranted = permissionMap.values.reduce { acc, next -> acc && next }
        if (allGranted) {

        } else {

        }
    }



    checkAndRequestPermissions(
        context = LocalContext.current,
        permissions = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        ),
        launcher = launcher
    )
}


fun checkAndRequestPermissions(
    context: Context,
    permissions: Array<String>,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    if (
        !permissions.all {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    ) {
        launcher.launch(permissions)
    }
}

private const val TAG = "MainActivity"

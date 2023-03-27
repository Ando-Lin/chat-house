@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ando.chathouse.constant.PreferencesKey
import com.ando.chathouse.ext.navigateSingleTop
import com.ando.chathouse.ui.component.SnackbarUI
import com.ando.chathouse.ui.screen.*
import com.ando.chathouse.ui.theme.ChatHouseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ChatHouse)
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            var enableDrawer by rememberSaveable {
                mutableStateOf(true)
            }
            val pagerState = rememberPagerState(MainScreen.defaultPage)
            Init()
            ChatHouseTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    snackbarHost = { SnackbarUI.ComposeUI() }
                ) { paddingValue ->
                    Surface(
                        modifier = Modifier
                            .padding(paddingValue)
                            .fillMaxSize(),
                    ) {
                        MainScreen.ScreenDrawer(
                            navigationRequest = {navController.navigateSingleTop(it)},
                            enableDrawer = enableDrawer,
                            pagerState = pagerState
                        ) {
                            Navigation(
                                pagerState = pagerState,
                                navController = navController,
                                enableDrawer = {
                                    enableDrawer = it
                                }
                            )
                        }
                    }
                }

            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    pagerState: PagerState,
    navController: NavHostController,
    enableDrawer: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = MainScreenDestination.routeWithArg,
    ) {
        composable(
            route = MainScreenDestination.routeWithArg,
            arguments = MainScreenDestination.arguments
        ) { navBackStackEntry ->
            enableDrawer(true)
            val arguments = navBackStackEntry.arguments
            val tab = arguments?.getString(MainScreenDestination.tabRoute) ?: ChatScreenTabDestination.route
            //更新参数
            val params = arguments?.getString(MainScreenDestination.tabParas)
            val context = LocalContext.current
            LaunchedEffect(params){
                val chatId = params?.toIntOrNull()?:return@LaunchedEffect
                context.profile.updateData {
                    val mutablePreferences = it.toMutablePreferences()
                    mutablePreferences[PreferencesKey.currentChatId]=chatId
                    mutablePreferences
                }
            }
            Log.i(TAG, "mainGraph: tab=$tab  paras=$params")
            MainScreen.ScreenUI(
                key = navBackStackEntry.arguments,
                requestTab = tab,
                pagerState = pagerState,
                navigationRequest = { navController.navigateSingleTop(it) }
            )
        }
        composable(route = SettingScreenDestination.route) {
            enableDrawer(false)
            SettingScreen(backAction = { navController.popBackStack() })
        }
        composable(
            route = ProfileScreenDestination.routeWithArg,
            arguments = ProfileScreenDestination.arguments
        ) {
            enableDrawer(false)
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

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.ando.chathouse

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ando.chathouse.constant.PreferencesKey
import com.ando.chathouse.ext.navigateSingleTop
import com.ando.chathouse.ui.component.LaunchedKeyEffect
import com.ando.chathouse.ui.component.SnackbarUI
import com.ando.chathouse.ui.screen.MainScreen
import com.ando.chathouse.ui.screen.ProfileScreen
import com.ando.chathouse.ui.screen.SettingScreen
import com.ando.chathouse.ui.theme.ChatHouseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置compose
        setContent {
            val navController = rememberNavController()
            val pagerState = rememberPagerState(MainScreen.defaultPage)
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
                            pagerState = pagerState
                        )
                    }
                }

            }
        }

    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    pagerState: PagerState
) {
    NavHost(
        navController = navController,
        startDestination = MainScreenDestination.routeWithArg,
    ) {
        composable(
            route = MainScreenDestination.routeWithArg,
            arguments = MainScreenDestination.arguments
        ) { navBackStackEntry ->
            val arguments = navBackStackEntry.arguments
            val tab = arguments?.getString(MainScreenDestination.tabRoute)
                ?: ChatScreenTabDestination.route
            //更新参数
            val params = arguments?.getString(MainScreenDestination.tabParas)
            val context = LocalContext.current
            LaunchedKeyEffect(params) {
                val chatId = params?.toIntOrNull()
                if (chatId != null) {
                    context.profile.updateData {
                        val mutablePreferences = it.toMutablePreferences()
                        mutablePreferences[PreferencesKey.currentChatId] = chatId
                        mutablePreferences
                    }
                }
            }
            MainScreen.ScreenUI(
                key = arguments,
                requestTab = tab,
                pagerState = pagerState,
                navigationRequest = { navController.navigateSingleTop(it) }
            )
        }
        composable(route = SettingScreenDestination.route) {
            SettingScreen(backAction = { navController.popBackStack() })
        }
        composable(
            route = ProfileScreenDestination.routeWithArg,
            arguments = ProfileScreenDestination.arguments
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

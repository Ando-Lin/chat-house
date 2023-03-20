@file:OptIn(ExperimentalMaterial3Api::class)

package com.ando.tastechatgpt

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.withStateAtLeast
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ando.tastechatgpt.constant.PreferencesKey
import com.ando.tastechatgpt.ext.navigateSingleTop
import com.ando.tastechatgpt.ui.component.SnackbarUI
import com.ando.tastechatgpt.ui.screen.*
import com.ando.tastechatgpt.ui.theme.TasteChatGPTTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TasteChatGPT)
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            var enableDrawer by rememberSaveable {
                mutableStateOf(true)
            }
            Init()
            TasteChatGPTTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    snackbarHost = { SnackbarUI.ComposeUI() }
                ) { paddingValue ->
                    Surface(
                        modifier = Modifier
                            .padding(paddingValue)
                            .fillMaxSize(),
                    ) {
                        MainScreenFramework(
                            navHostController = navController,
                            visualDestinationList = navigationBatDestinations,
                            startDestination = ChatScreenDestination.routeWithArg,
                            enableDrawer = enableDrawer,
                            drawerState = drawerState,

                            ) {
                            Navigation(
                                navController = navController,
                                drawerState = drawerState,
                                enableDrawer = { enableDrawer = it }
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
    navController: NavHostController,
    drawerState: DrawerState,
    enableDrawer: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "main",
    ) {
        mainGraph(navController, drawerState, enableDrawer)
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

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    drawerState: DrawerState,
    enableDrawer: (Boolean) -> Unit
) {
    navigation(startDestination = ChatScreenDestination.routeWithArg, route = "main") {
        composable(
            route = ChatScreenDestination.routeWithArg,
            arguments = ChatScreenDestination.arguments
        ) { navBackStackEntry ->
            enableDrawer(true)
            ChatScreen(
                drawerState = drawerState,
                vm = hiltViewModel(navBackStackEntry),
                navigationAction = {
                    navController.navigateSingleTop(it)
                }
            )
        }
        composable(route = RoleListScreenDestination.route) { navBackStackEntry ->
            enableDrawer(true)
            RoleListScreen(
                drawerState = drawerState,
                navigateAction = { navController.navigateSingleTop(it) },
                viewModel = hiltViewModel(navBackStackEntry)
            )
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

private val navigationBatDestinations =
    listOf(ChatScreenDestination, RoleListScreenDestination).toMutableStateList()

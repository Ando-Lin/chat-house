package com.ando.chathouse

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Destination(val route: String)
sealed class VisualTabDestination(
    route: String,
    val labelResId: Int,
    val selectedIconResId: Int,
    val unselectedIconResId: Int
) : Destination(route = route)

object ChatScreenTabDestination : VisualTabDestination(
    route = "chat_screen",
    labelResId = R.string.chat,
    selectedIconResId = R.drawable.ic_chat_filled,
    unselectedIconResId = R.drawable.ic_chat_outline
) {
    const val argName = "chatId"

    fun routeWithArg(chatId: Int?) = MainScreenDestination.routeWithArg(route, chatId?.toString())
}

object SettingScreenDestination : Destination(route = "setting_screen")

object NightModeScreenDestination : Destination(route = "night_mode_screen")

object ProfileScreenDestination : Destination(route = "profile_screen") {
    const val argName = "userId"
    val routeWithArg = "$route/{$argName}"
    val arguments = listOf(navArgument(name = argName) {
        this.type = NavType.IntType
    })

    fun routeWithArg(uid: Int) = "$route/$uid"
}

object RoleListScreenTabDestination : VisualTabDestination(
    route = "role_list_screen",
    labelResId = R.string.role_list,
    selectedIconResId = R.drawable.ic_role_filled,
    unselectedIconResId = R.drawable.ic_role_outline
)

object MainScreenDestination : Destination(
    route = "main_screen"
) {
    const val tabRoute = "tab"
    const val tabParas = "paras"
    val routeWithArg = "$route/{$tabRoute}?$tabParas={$tabParas}"
    val arguments = listOf(
        navArgument(tabRoute) {
            type = NavType.StringType
            defaultValue = ChatScreenTabDestination.route
        },
        navArgument(tabParas) {
            type = NavType.StringType
            nullable = true
        }
    )

    fun routeWithArg(tabRoute: String, paras: String?=null) =
        paras?.let { "$route/$tabRoute?${this.tabParas}=$paras" }?:"$route/$tabRoute"
}
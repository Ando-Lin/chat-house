package com.ando.tastechatgpt

import android.util.Log
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Destination(val route: String)
sealed class VisualDestination(
    route: String,
    val labelResId: Int,
    val selectedIconResId: Int,
    val unselectedIconResId: Int
) : Destination(route = route){
    abstract val realRoute:String
}

object ChatScreenDestination : VisualDestination(
    route = "chatScreen",
    labelResId = R.string.chat,
    selectedIconResId = R.drawable.ic_chat_filled,
    unselectedIconResId = R.drawable.ic_chat_outline
) {
    const val argName = "chatId"
    val routeWithArg = "$route?$argName={$argName}"
    val arguments = listOf(navArgument(name = argName) {
        nullable = true
    })
    override val realRoute: String
        get() = routeWithArg

    fun routeWithArg(chatId: Any?) = chatId?.let { "$route?$argName=$it" } ?: route
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

object RoleListScreenDestination : VisualDestination(
    route = "role_list_screen",
    labelResId = R.string.role_list,
    selectedIconResId = R.drawable.ic_role_filled,
    unselectedIconResId = R.drawable.ic_role_outline
) {
    override val realRoute: String
        get() = route
}
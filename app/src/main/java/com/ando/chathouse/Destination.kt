package com.ando.chathouse

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Destination(val route: String)
sealed class VisualTabDestination(
    route: String,
    val labelResId: Int,
    val selectedIconResId: Int,
    val unselectedIconResId: Int,
) : Destination(route = route){
    abstract val realRoute:String
}

object ChatScreenTabDestination : VisualTabDestination(
    route = "chat_screen",
    labelResId = R.string.chat,
    selectedIconResId = R.drawable.ic_chat_filled,
    unselectedIconResId = R.drawable.ic_chat_outline
) {
    const val argName = "chatId"
    val routeWithArg = "$route?$argName={$argName}"
    override val realRoute: String
        get() = routeWithArg
    val arguments = listOf(
        navArgument(name = argName){
            type = NavType.StringType
            nullable = true
        }
    )
    fun routeWithArg(chatId: Int?) =  chatId?.let { "$route?$argName=$chatId" }?: route
}

object SettingScreenDestination : Destination(route = "setting_screen")


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
    unselectedIconResId = R.drawable.ic_role_outline,
){
    override val realRoute: String
        get() = route
}

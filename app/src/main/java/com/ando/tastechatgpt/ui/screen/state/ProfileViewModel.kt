package com.ando.tastechatgpt.ui.screen.state

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ando.tastechatgpt.ProfileScreenDestination
import com.ando.tastechatgpt.data.repo.UserRepo
import com.ando.tastechatgpt.domain.entity.toUser
import com.ando.tastechatgpt.domain.pojo.User
import com.ando.tastechatgpt.domain.pojo.toUserEntity
import com.ando.tastechatgpt.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

private const val TAG = "ProfileViewModel"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context,
    private val savedState: SavedStateHandle
) : ViewModel() {
    //uid=0时将会插入新记录
    val uid: Int = savedState[ProfileScreenDestination.argName] ?: 0

    //未知用户
    private val unknownUser: User = User(id = 0, name = "", avatar = null, description = "")

    //屏幕ui状态
    var screenUiState: ProfileScreenUiState by mutableStateOf(
        ProfileScreenUiState(user = unknownUser)
    )
        private set

    //初始化user变量
    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            uid.let { value ->
                userRepo
                    .fetchById(value)
                    .map { it?.toUser() }
                    .onEach {
                        it?.let {
                            screenUiState = screenUiState.copy(user = it, tempUser = it)
                        }
                    }
                    .catch { updateMessage("获取用户时发生错误: ${it.message}") }
                    .collect()
            }
        }
    }

    fun saveUser() {
        viewModelScope.launch {
            var lastUser = screenUiState.tempUser
            val user = screenUiState.user
            //若头像被修改了则将图片移动至私有文件夹
            if (lastUser.avatar != user?.avatar) {
                val newUri = copyPictureToPrivateFolder(lastUser.avatar)
                lastUser = lastUser.copy(avatar = newUri)
                updateTempUser(tempUser = lastUser)
            }
            kotlin.runCatching {
                if (lastUser.id == 0) {
                    userRepo.save(lastUser.toUserEntity(LocalDateTime.now()))
                } else {
                    userRepo.update(lastUser)
                }
            }
                .onFailure { updateMessage("更新用户时发生错误: ${it.message}") }
                .onSuccess {
                    //更新数据库user, 重置isModified
                    screenUiState = screenUiState.copy(user = lastUser)
                    updateIsModified(false)
                }
        }
    }

    private fun updateIsModified(state: Boolean) {
        screenUiState = screenUiState.copy(isModified = state)
    }

    private fun updateMessage(message: String) {
        screenUiState = screenUiState.copy(message = message)
    }

    fun updateTempUser(tempUser: User) {
        screenUiState = screenUiState.copy(tempUser = tempUser, isModified = true)
        updateIsModified(screenUiState.user != screenUiState.tempUser)
    }

    fun copyPictureToPrivateFolder(uri: Uri?): Uri? {
        uri ?: return null
        val filename: String = Instant.now().toEpochMilli().toString()
        val folder = File(context.filesDir, "/avatar")
        if (!folder.exists()){
            folder.mkdir()
        }
        val newFile = File(context.filesDir, "/avatar/$filename")
        return Utils.copyFile(context, uri, newFile)
    }

    fun copyPictureToCacheFolder(uri: Uri?): Uri? {
        uri ?: return null
        val filename: String = Instant.now().toEpochMilli().toString()
        val folder = File(context.cacheDir, "/avatar")
        if (!folder.exists()){
            folder.mkdir()
        }
        val newFile = File(context.cacheDir, "/avatar/$filename")
        return Utils.copyFile(context, uri, newFile)
    }

}


@Stable
data class ProfileScreenUiState(
    val user: User,
    val tempUser: User = user,
    val isModified: Boolean = false,
    val message: String = ""
)
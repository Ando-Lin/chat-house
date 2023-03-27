package com.ando.tastechatgpt.ui.screen.state

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ando.tastechatgpt.ProfileScreenDestination
import com.ando.tastechatgpt.data.repo.UserRepo
import com.ando.tastechatgpt.domain.entity.toUserDetail
import com.ando.tastechatgpt.domain.pojo.UserDetail
import com.ando.tastechatgpt.domain.pojo.toEntity
import com.ando.tastechatgpt.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import javax.inject.Inject

private const val TAG = "ProfileViewModel"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    //uid=0时将会插入新记录
    val uid: Int = savedStateHandle[ProfileScreenDestination.argName] ?: 0

    //未知用户
    private val unknownUser: UserDetail = UserDetail()

    private var user: UserDetail = unknownUser

    //屏幕ui状态
    private val _extraUiStateState = mutableStateOf(
        ProfileExtraSettingUiState(
            reminder = user.reminder,
            enableRoleGuide = user.enableGuide,
            enableReminderMode = user.enableReminder
        )
    )
    private var extraUiState by _extraUiStateState
    var screenUiState by mutableStateOf(
        ProfileScreenUiState(tempUser = user, extraSettingUiStateState = _extraUiStateState)
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
                    .map { it?.toUserDetail() }
                    .onEach {
                        it?.let {
                            user = it
                            screenUiState = screenUiState.copy(tempUser = it)
                            extraUiState = extraUiState.copy(
                                reminder = it.reminder,
                                enableRoleGuide = it.enableGuide,
                                enableReminderMode = it.enableReminder
                            )
                        }
                    }
                    .catch { updateMessage("获取用户时发生错误: ${it.message}") }
                    .first()
            }
        }
    }

    fun resetMessage(){
        screenUiState = screenUiState.copy(message = "")
    }

    fun saveUser() {
        viewModelScope.launch {
            var lastUser = screenUiState.tempUser

            //若头像被修改了则将图片移动至私有文件夹
            if (lastUser.avatar != user.avatar) {
                val newUri = copyPictureToPrivateFolder(lastUser.avatar)
                lastUser = lastUser.copy(avatar = newUri)
                updateTempUser(tempUser = lastUser)
            }

            val result = if (lastUser.id==0){
                userRepo.save(lastUser.toEntity())
                    .onSuccess {
                        //更新id，防止多次保存新user
                        lastUser = lastUser.copy(id = it)
                        updateTempUser(lastUser)
                    }
            }else{
                userRepo.update(lastUser.toEntity())
            }

            result
                .onFailure { updateMessage("更新用户时发生错误: ${it.message}") }
                .onSuccess {
                    //更新数据库user, 重置isModified
                    user = lastUser
                    updateIsModified(false)
                }
        }
    }

    fun updateRoleGuideEnableState(state: Boolean) {
        extraUiState = extraUiState.copy(enableRoleGuide = state)
        user = user.copy(enableGuide = state)
        updateUser(user)
    }

    fun updateReminderModeEnableState(state: Boolean) {
        extraUiState = extraUiState.copy(enableReminderMode = state)
        user = user.copy(enableReminder = state)
        updateUser(user)
    }

    private fun updateUser(user: UserDetail) {
        viewModelScope.launch {
            userRepo.update(user.toEntity())
                .onFailure { updateMessage("更新用户时发生错误: ${it.message}") }
        }
    }

    fun updateReminder(value: String) {
        if (value == user.reminder) return
        user = user.copy(reminder = value)
        updateUser(user)
    }

    private fun updateIsModified(state: Boolean) {
        screenUiState = screenUiState.copy(isModified = state)
    }

    private fun updateMessage(message: String) {
        screenUiState = screenUiState.copy(message = message)
    }

    fun updateTempUser(tempUser: UserDetail) {
        screenUiState = screenUiState.copy(tempUser = tempUser, isModified = true)
        updateIsModified(user != screenUiState.tempUser)
    }

    private fun copyPictureToPrivateFolder(uri: Uri?): Uri? {
        uri ?: return null
        val filename: String = Instant.now().toEpochMilli().toString()
        val folder = File(context.filesDir, "/avatar")
        if (!folder.exists()) {
            folder.mkdir()
        }
        val newFile = File(context.filesDir, "/avatar/$filename")
        return Utils.copyFile(context, uri, newFile)
    }

    fun copyPictureToCacheFolder(uri: Uri?): Uri? {
        uri ?: return null
        val filename: String = Instant.now().toEpochMilli().toString()
        val folder = File(context.cacheDir, "/avatar")
        if (!folder.exists()) {
            folder.mkdir()
        }
        val newFile = File(context.cacheDir, "/avatar/$filename")
        return Utils.copyFile(context, uri, newFile)
    }


}


@Stable
data class ProfileScreenUiState(
    val tempUser: UserDetail,
    val isModified: Boolean = false,
    val message: String = "",
    private val extraSettingUiStateState: State<ProfileExtraSettingUiState>
) {
    val extraSettingUiState by extraSettingUiStateState
}

data class ProfileExtraSettingUiState(
    val reminder: String,
    val enableRoleGuide: Boolean,
    val enableReminderMode: Boolean,
)
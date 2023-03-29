package com.ando.chathouse.ui.screen.state

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ando.chathouse.ProfileScreenDestination
import com.ando.chathouse.data.repo.UserRepo
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.domain.pojo.UserExtraInfo
import com.ando.chathouse.ext.toUserAndExtras
import com.ando.chathouse.ext.toUserEntity
import com.ando.chathouse.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    //uid=0时将会插入新记录
    val uid: Int = savedStateHandle[ProfileScreenDestination.argName] ?: 0


    //记录最新接收的user.手动更新值
    private var latestUser: User = User.emptyUser

    private val _extrasInfoState = mutableStateOf(UserExtraInfo(id = 0))
    //记录最新状态，手动更新值
    private var extrasInfoState by _extrasInfoState
    //ui状态
    private val _extraUiStateState = mutableStateOf(
        ProfileExtraSettingUiState(
            userExtraInfo = _extrasInfoState,
            latestReminder = "",
        )
    )
    private var extraUiState by _extraUiStateState
    var screenUiState by mutableStateOf(
        ProfileScreenUiState(tempUser = latestUser, extraSettingUiStateState = _extraUiStateState)
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
                    .map { it?.toUserAndExtras() }
                    .onEach {
                        it?.let { (user, extras) ->
                            screenUiState = screenUiState.copy(tempUser = user)
                            extrasInfoState = extras
                            extraUiState = extraUiState.copy(latestReminder = extras.reminder)
                        }
                    }
                    .catch { updateMessage("获取用户时发生错误: ${it.message}") }
                    .first()
            }
        }
    }

    fun resetMessage() {
        screenUiState = screenUiState.copy(message = "")
    }

    //保存或更新用户
    fun saveUser() {
        viewModelScope.launch {
            var latestUser = screenUiState.tempUser

            //若头像被修改了则将图片移动至私有文件夹
            if (latestUser.avatar != this@ProfileViewModel.latestUser.avatar) {
                val newUri = copyPictureToPrivateFolder(latestUser.avatar)
                latestUser = latestUser.copy(avatar = newUri)
                updateTempUser(tempUser = latestUser)
            }

            val result = if (latestUser.id == 0) {
                userRepo.save(latestUser.toUserEntity(LocalDateTime.now()))
                    .onSuccess {
                        //更新id，防止多次保存新user
                        latestUser = latestUser.copy(id = it)
                        updateTempUser(latestUser)
                    }
            } else {
                userRepo.update(user = latestUser)
            }

            result
                .onFailure { updateMessage("更新用户时发生错误: ${it.message}") }
                .onSuccess {
                    //更新数据库user, 重置isModified
                    this@ProfileViewModel.latestUser = latestUser
                    updateUserIsModified(false)
                }
        }
    }

    fun updateRoleGuideEnableState(state: Boolean) {
        extrasInfoState = extrasInfoState.copy(enableGuide = state)
        updateUserExtrasInfo(extrasInfoState)
    }

    fun updateReminderModeEnableState(state: Boolean) {
        extrasInfoState = extrasInfoState.copy(enableReminder = state)
        updateUserExtrasInfo(extrasInfoState)
    }


    private fun updateUserExtrasInfo(extras: UserExtraInfo) {
        viewModelScope.launch {
            userRepo.update(extrasInfo = extras)
                .onFailure { updateMessage("更新用户时发生错误: ${it.message}") }
        }
    }

    //用于更新界面，不写入数据库
    fun updateLatestReminder(value: String) {
        extraUiState = extraUiState.copy(latestReminder = value)
    }

    //将reminder写入数据库
    fun updateReminder() {
        if (extraUiState.latestReminder == extrasInfoState.reminder) return
        extrasInfoState = extrasInfoState.copy(reminder = extraUiState.latestReminder)
        updateUserExtrasInfo(extrasInfoState)
    }

    private fun updateUserIsModified(state: Boolean) {
        screenUiState = screenUiState.copy(isModified = state)
    }

    private fun updateMessage(message: String) {
        screenUiState = screenUiState.copy(message = message)
    }

    fun updateTempUser(tempUser: User) {
        screenUiState = screenUiState.copy(tempUser = tempUser, isModified = true)
        updateUserIsModified(latestUser != screenUiState.tempUser)
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
    val tempUser: User, //作为user的写缓存
    val isModified: Boolean = false,    //控制重复更新行为
    val message: String = "",
    private val extraSettingUiStateState: State<ProfileExtraSettingUiState>
) {
    val extraSettingUiState by extraSettingUiStateState
}

data class ProfileExtraSettingUiState(
    private val userExtraInfo: State<UserExtraInfo>,
    val latestReminder: String = userExtraInfo.value.reminder,//避免频繁写入更新，作为reminder写入缓存
) {
    val info by userExtraInfo
}
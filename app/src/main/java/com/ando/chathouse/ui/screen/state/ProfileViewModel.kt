package com.ando.chathouse.ui.screen.state

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ando.chathouse.ProfileScreenDestination
import com.ando.chathouse.data.repo.UserRepo
import com.ando.chathouse.domain.entity.toUserDetail
import com.ando.chathouse.domain.pojo.UserDetail
import com.ando.chathouse.domain.pojo.toEntity
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
            originalReminder = user.reminder,
            latestReminder = user.reminder,
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
                            extraUiState = ProfileExtraSettingUiState(
                                originalReminder = it.reminder,
                                latestReminder = it.reminder,
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
            var latestUser = screenUiState.tempUser

            //若头像被修改了则将图片移动至私有文件夹
            if (latestUser.avatar != user.avatar) {
                val newUri = copyPictureToPrivateFolder(latestUser.avatar)
                latestUser = latestUser.copy(avatar = newUri)
                updateTempUser(tempUser = latestUser)
            }

            val result = if (latestUser.id==0){
                userRepo.save(latestUser.toEntity())
                    .onSuccess {
                        //更新id，防止多次保存新user
                        latestUser = latestUser.copy(id = it)
                        updateTempUser(latestUser)
                    }
            }else{
                userRepo.update(latestUser.toEntity())
            }

            result
                .onFailure { updateMessage("更新用户时发生错误: ${it.message}") }
                .onSuccess {
                    //更新数据库user, 重置isModified
                    user = latestUser
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

    fun updateLatestReminder(value:String){
        extraUiState = extraUiState.copy(latestReminder = value)
    }

    fun updateReminder() {
        if (extraUiState.latestReminder == extraUiState.originalReminder) return
        user = user.copy(reminder = extraUiState.latestReminder)
        extraUiState = extraUiState.copy(originalReminder = extraUiState.latestReminder)
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
    val originalReminder: String,
    val latestReminder: String,
    val enableRoleGuide: Boolean,
    val enableReminderMode: Boolean,
)
package com.ando.tastechatgpt.ui.screen.state

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.ando.tastechatgpt.ChatScreenDestination
import com.ando.tastechatgpt.constant.HUMAN_UID
import com.ando.tastechatgpt.constant.PreferencesKey
import com.ando.tastechatgpt.data.repo.ChatRepo
import com.ando.tastechatgpt.data.repo.UserRepo
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.ChatMessage
import com.ando.tastechatgpt.model.ChatModelManger
import com.ando.tastechatgpt.profile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepo,
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context,
    private val chatModelManger: ChatModelManger,
    private val savedStateHandle: SavedStateHandle,
    private val external: CoroutineScope
) : ViewModel() {
    private val chatIdFromBundle =
        (savedStateHandle.get<String>(ChatScreenDestination.argName))?.toInt()
    private var chatId: Int = chatIdFromBundle ?: -1
    private val myId = HUMAN_UID

    var bottomBarUiState by mutableStateOf(ChatBottomBarUiState(""))
        private set
    private val _availableModels = chatRepo.availableModelList
    private var title by mutableStateOf("Unknown")
    private val currentModelFlow: Flow<String?>
        get() = context.profile.data.map { it[PreferencesKey.currentModel] }
    private val currentChatIdFlow: Flow<Int?>
        get() = context.profile.data.map { it[PreferencesKey.currentChatId] }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        updateUiMessage(throwable.message?:"发生异常")
        Log.e(TAG, "协程异常: ", throwable)
    }


    var screenUiState by mutableStateOf(
        ChatScreenUiState(
            title = title,
            chatId = chatId,
            availableModels = _availableModels,
            currentModel = currentModelFlow,
            flowPagingData = getPagingData(chatId)
        )
    )
        private set


    init {
        if (chatIdFromBundle != null) {
            updateCurrentChatId(chatId = chatIdFromBundle)
            resetPartialScreenUiState(chatId)
        } else {
            viewModelScope.launch {
                currentChatIdFlow.collect {
                    if (it == null) return@collect
                    chatId = it
                    resetPartialScreenUiState(chatId)
                    //延迟读取
                    
                }
            }
        }
    }

    private fun resetPartialScreenUiState(chatId: Int) {
        viewModelScope.launch {
            userRepo.fetchById(chatId)
                .onEach { Log.i(TAG, "getUser: $it") }
                .onEach { updateTitle(it?.name) }
                .catch { updateUiMessage("加载用户失败: ${it.message}") }
                .collect()
        }
        if (chatId!=chatIdFromBundle){
            screenUiState = screenUiState.copy(flowPagingData = getPagingData(chatId))
        }
    }


    private fun updateCurrentChatId(chatId: Int?) {
        chatId ?: return
        viewModelScope.launch {
            context.profile.updateData {
                val mutablePreferences = it.toMutablePreferences()
                mutablePreferences[PreferencesKey.currentChatId] = chatId
                mutablePreferences
            }
        }
    }

    private fun updateTitle(title: String?) {
        title ?: return
        screenUiState = screenUiState.copy(title = title)
    }

    fun updateCurrentModel(modelName: String) {
        external.launch {
            context.profile.updateData {
                val mutablePreferences = it.toMutablePreferences()
                mutablePreferences[PreferencesKey.currentModel] = modelName
                return@updateData mutablePreferences
            }
        }
    }

    fun updateTextFieldValue(value: String) {
        bottomBarUiState = bottomBarUiState.copy(textValue = value)
    }


    fun updateMessageContent(id: Int, content: String) {
        chatRepo.update(id = id, msg = content)
    }

    fun updateUiMessage(message: String){
        screenUiState = screenUiState.copy(message = message)
    }

    /**
     * 发送消息
     */
    fun sendMessage(modelName: String, msg: String, previousMsgTime: LocalDateTime?) {
        val now = LocalDateTime.now();
        val last = previousMsgTime ?: LocalDateTime.MIN
        val secondDiff = Duration.between(last, now).seconds
        val message = ChatMessage(
            chatId = chatId,
            uid = myId,
            text = msg,
            timestamp = now,
            secondDiff = secondDiff,
            status = MessageStatus.Sending
        )
        //消息发送
        kotlin.runCatching {
            chatRepo.sendMessage(message = message, modelName = modelName)
        }.onFailure { updateUiMessage("消息发送失败：${it.message}") }
        //重置输入框文本
        updateTextFieldValue("")
    }

    /**
     * 重发送消息
     */
    fun resendMessage(
        modelName: String,
        msgId: Int,
        previousMsgTime: LocalDateTime?,
        msg: String
    ) {
        this.deleteMessage(msgId)
        this.sendMessage(modelName, msg, previousMsgTime)
    }

    /**
     * 删除消息
     */
    fun deleteMessage(msgId: Int) {
        kotlin.runCatching {
            chatRepo.deleteMessage(msgId)
        }.onFailure { updateUiMessage("删除消息失败：${it.message}") }
    }

    /**
     * 获取pagingData
     */
    private fun getPagingData(chatId: Int): Flow<PagingData<ChatEntryUiState>> {
        return Pager(
            config = PagingConfig(pageSize = 25)
        ) {
            chatRepo.getPagingSourceByChatId(chatId)
        }
            .flow
            .map { pagingData ->
                pagingData.map { value ->
                    val userFlow = userRepo.fetchById(id = value.uid)
                    val user = userFlow.first()
                    this.title = user!!.name
                    ChatEntryUiState(
                        entity = value,
                        avatar = user.avatar
                    )
                }
            }
            .catch { updateUiMessage("获取分页数据失败：${it.message}") }
            .cachedIn(viewModelScope)
    }
}

data class ChatEntryUiState(
    private val entity: ChatMessageEntity,
    val avatar: Uri?,
    val status: MessageStatus = entity.status
) {
    val id: Int
        get() = entity.id
    val uid: Int
        get() = entity.uid
    val text: String
        get() = entity.text
    val timestamp: LocalDateTime
        get() = entity.timestamp
    val secondDiff: Long
        get() = entity.secondDiff
}

data class ChatScreenUiState(
    val title: String,
    val chatId: Int,
    val availableModels: List<String>,
    val currentModel: Flow<String?>,
    val flowPagingData: Flow<PagingData<ChatEntryUiState>>,
    val message: String = "",
    val myId: Int = HUMAN_UID
)

data class ChatBottomBarUiState(
    val textValue: String
)

private const val TAG = "ChatViewModel"
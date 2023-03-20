@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ando.tastechatgpt.ui.screen.state

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.ando.tastechatgpt.MainScreenDestination
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    private val myId = HUMAN_UID
    private val currentModelFlow: Flow<String?>
        get() = context.profile.data.map { it[PreferencesKey.currentModel] }
    private val currentChatIdFlow: Flow<Int?>
        get() = context.profile.data.map { it[PreferencesKey.currentChatId] }
    private val chatIdFlow: SharedFlow<Int> = flow {
        val flow = savedStateHandle
            .getStateFlow(MainScreenDestination.tabParas, "")
            .onEach { Log.i(TAG, "stateflow.value=$it ") }
            .combine(currentChatIdFlow.distinctUntilChanged()) { v1, v2 ->
                Log.i(TAG, "flow bine: v1=$v1, v2=$v2")
                return@combine when {
                    v1.isNotBlank() -> {
                        val id = v1.toIntOrNull()?:-1
                        if (id != v2) updateCurrentChatId(id)
                        id
                    }
                    v2 != null -> v2
                    else -> -1
                }
            }
            .distinctUntilChanged()
        emitAll(flow)
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

    var bottomBarUiState by mutableStateOf(ChatBottomBarUiState(""))
        private set
    private val _availableModels = chatRepo.availableModelList
    private var titleFlow = chatIdFlow
        .flatMapLatest { userRepo.fetchById(it) }
        .map { it?.name ?: "" }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        updateUiMessage(throwable.message ?: "发生异常")
        Log.e(TAG, "协程异常: ", throwable)
    }


    var screenUiState by mutableStateOf(
        ChatScreenUiState(
            titleFlow = titleFlow,
            availableModels = _availableModels,
            currentModel = currentModelFlow,
            flowPagingData = getPagingData()
        )
    )
        private set


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

    fun updateUiMessage(message: String) {
        screenUiState = screenUiState.copy(message = message)
    }

    /**
     * 发送消息
     */
    fun sendMessage(modelName: String, msg: String, previousMsgTime: LocalDateTime?) {
        val chatId = chatIdFlow.replayCache[0]
        if (chatId==-1){
            updateUiMessage("角色不存在")
            return
        }
        val now = LocalDateTime.now()
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
    private fun getPagingData(): Flow<Flow<PagingData<ChatMessageUiState>>> {
        return chatIdFlow.mapLatest {chatId->
            if (chatId==-1){
                return@mapLatest flowOf(PagingData.empty())
            }
            Pager(
                config = PagingConfig(pageSize = 25)
            ) {
                chatRepo.getPagingSourceByChatId(chatId)
            }
                .flow
                .map { pagingData ->
                    pagingData.map { value ->
                        val userFlow = userRepo.fetchById(id = value.uid)
                        val user = userFlow.first()
                        ChatMessageUiState(
                            entity = value,
                            avatar = user?.avatar
                        )
                    }
                }
                .catch { updateUiMessage("获取分页数据失败：${it.message}") }
                .cachedIn(viewModelScope)
        }
        }
}

data class ChatMessageUiState(
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
    val titleFlow: Flow<String>,
    val availableModels: List<String>,
    val currentModel: Flow<String?>,
    val flowPagingData: Flow<Flow<PagingData<ChatMessageUiState>>>,
    val message: String = "",
    val myId: Int = HUMAN_UID
)

data class ChatBottomBarUiState(
    val textValue: String
)

private const val TAG = "ChatViewModel"
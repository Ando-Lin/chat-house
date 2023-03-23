@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ando.tastechatgpt.ui.screen.state

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import com.ando.tastechatgpt.MainScreenDestination
import com.ando.tastechatgpt.constant.HUMAN_UID
import com.ando.tastechatgpt.constant.PreferencesKey
import com.ando.tastechatgpt.data.repo.ChatRepo
import com.ando.tastechatgpt.data.repo.UserRepo
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.ChatMessage
import com.ando.tastechatgpt.domain.pojo.toEntity
import com.ando.tastechatgpt.model.ChatModelManger
import com.ando.tastechatgpt.profile
import com.ando.tastechatgpt.ui.component.BubbleTextUiState
import com.ando.tastechatgpt.ui.component.exclusive.ChatScreenBottomBarUiState
import com.ando.tastechatgpt.ui.component.exclusive.ChatScreenSettingsUiState
import com.ando.tastechatgpt.ui.component.exclusive.ChatScreenTopBarUiState
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

    //从配置读取当前模型
    private val currentModelFlow: StateFlow<String>
        get() = context.profile.data.map { it[PreferencesKey.currentModel] ?: "" }
            .stateIn(viewModelScope, SharingStarted.Lazily, "")

    //从配置读取当前chatId
    private val currentChatIdFlow: StateFlow<Int?>
        get() = context.profile.data.map { it[PreferencesKey.currentChatId] }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    //从savedStateHandle和配置中读取当前chatId. 以savedStateHandle的值为主
    private val chatIdFlow: StateFlow<Int> = savedStateHandle
        .getStateFlow(MainScreenDestination.tabParas, "")
        .onEach { Log.i(TAG, "stateflow.value=$it ") }
        .combine(currentChatIdFlow) { v1, v2 ->
            Log.i(TAG, "flow bine: v1=$v1, v2=$v2")
            return@combine when {
                v1.isNotBlank() -> {
                    val id = v1.toIntOrNull() ?: -1
                    if (id != v2) updateCurrentChatId(id)
                    id
                }
                v2 != null -> v2
                else -> -1
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, -1)


    private val _availableModels = chatRepo.availableModelList

    private var titleFlow = chatIdFlow
        .flatMapLatest { userRepo.fetchById(it) }
        .map { it?.name ?: "" }


    private val editModeState = mutableStateOf(false)
    private val multiSelectMode = mutableStateOf(false)

    private val settingsUiState = mutableStateOf(
        ChatScreenSettingsUiState(
            modelListFlow = flowOf(_availableModels),
            strategyListFlow = emptyFlow(), //TODO: 策略列表
            currentModelFlow = currentModelFlow,
            currentStrategyFlow = emptyFlow(), //TODO: 当前策略
            editModeState = editModeState,
            multiSelectModeState = multiSelectMode
        )
    )


    private val topBarUiState = mutableStateOf(
        ChatScreenTopBarUiState(
            titleFlow = titleFlow,
            settingsUiStateState = settingsUiState,
        )
    )

    private val bottomBarUiState =
        mutableStateOf(
            ChatScreenBottomBarUiState(
                editModeState = editModeState,
                multiSelectModeState = multiSelectMode
            )
        )

    var screenUiState by mutableStateOf(
        ChatScreenUiState(
            flowFlowPagingData = getPagingData(),
            myId = myId,
            topBarUiStateState = topBarUiState,
            bottomBarUiStateState = bottomBarUiState
        )
    )
        private set

    //保存多选模式下选中的消息id
    private val selectedMessageId: MutableList<Int> = mutableListOf()


    /**
     * 更新当前chatId. 写入到datastore中
     */
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


    /**
     * 更新当前模型
     */
    fun updateCurrentModel(modelName: String) {
        external.launch {
            context.profile.updateData {
                val mutablePreferences = it.toMutablePreferences()
                mutablePreferences[PreferencesKey.currentModel] = modelName
                return@updateData mutablePreferences
            }
        }
    }

    /**
     * 更新底部输入框的文字
     */
    fun updateInputText(value: String) {
        bottomBarUiState.value = bottomBarUiState.value.copy(text = value)
    }

    /**
     * 更新消息内容
     */
    fun updateMessageContent(id: Int, content: String) {
        viewModelScope.launch {
            chatRepo.update(id = id, msg = content)
                .onFailure { updateUiMessage("更新消息异常: ${it.message}") }
        }
    }

    /**
     * 更新用于通知的消息
     */
    private fun updateUiMessage(message: String) {
        screenUiState = screenUiState.copy(message = message)
    }

    /**
     * 更新策略
     */
    fun updateStrategy(strategy: String) {
        TODO()
    }

    /**
     * 清除对话
     */
    fun clearConversation() {
        //获取当前chatId
        val chatId = chatIdFlow.value
        //TODO: 处理
    }

    /**
     * 将选中的消息id在发送时携带
     */
    fun selectedToCarry() {
        //TODO: 处理
        selectedMessageId.clear()
    }

    /**
     * 将选中的消息id在发送时删除
     */
    fun selectedToExclude() {
        //TODO: 处理
        selectedMessageId.clear()
    }

    /**
     * 将选中的消息id删除
     */
    fun selectedTODelete() {
        //TODO: 处理
        selectedMessageId.clear()
    }

    /**
     * 收集选中的消息id
     */
    fun collectSelectedId(messageId: Int) {
        if (multiSelectMode.value) {
            selectedMessageId.add(messageId)
        }
    }

    /**
     * 转变多选模式
     */
    fun switchMultiSelectModeState(state: Boolean? = null) {
        val targetState = state ?: !(settingsUiState.value.multiSelectMode)
        multiSelectMode.value = targetState
    }

    /**
     * 转变阅读模式
     */
    fun switchEditModeState(state: Boolean? = null) {
        val targetState = state ?: !(settingsUiState.value.editMode)
        editModeState.value = targetState
    }

    /**
     * 发送消息
     */
    fun sendMessage(msg: String, previousMsgTime: LocalDateTime?, fromMe: Boolean = true) {
        //
        val editMode = settingsUiState.value.editMode
        //获取当前chatId
        val chatId = chatIdFlow.value
        if (chatId == -1) {
            updateUiMessage("角色不存在")
            return
        }

        //计算当前时间与上个消息的时间差
        val now = LocalDateTime.now()
        val last = previousMsgTime ?: LocalDateTime.MIN
        val secondDiff = Duration.between(last, now).seconds

        //创建消息实例
        val message = ChatMessage(
            chatId = chatId,
            uid = if (fromMe) myId else chatId,
            text = msg,
            timestamp = now,
            secondDiff = secondDiff,
            status = if (editMode) MessageStatus.Success else MessageStatus.Sending
        )

        //是否是编辑模式
        if (settingsUiState.value.editMode) {
            //异常处理
            //编辑模式下并不发送消息，而是直接插入数据库中
            viewModelScope.launch {
                chatRepo.save(message.toEntity())
                    .onFailure { updateUiMessage("保存消息异常：${it.message}") }
            }
        } else {
            //获取当前模型
            val currentModel = currentModelFlow.value

            //消息发送
            viewModelScope.launch {
                chatRepo.sendMessage(message = message, modelName = currentModel)
                    .onFailure { updateUiMessage("消息发送失败：${it.message}") }
            }
        }

        //重置输入框文本
        updateInputText("")
    }

    /**
     * 重发送消息
     */
    fun resendMessage(
        msgId: Int,
        previousMsgTime: LocalDateTime?,
        msg: String
    ) {
        this.deleteMessage(msgId)
        this.sendMessage(msg, previousMsgTime)
    }

    /**
     * 删除消息
     */
    fun deleteMessage(msgId: Int) {
        viewModelScope.launch {
            chatRepo.deleteMessage(msgId)
                .onFailure { updateUiMessage("删除消息失败：${it.message}") }
        }
    }

    /**
     * 获取pagingData
     */
    private fun getPagingData(): Flow<Flow<PagingData<ChatMessageUiState>>> {
        return chatIdFlow.mapLatest { chatId ->
            if (chatId == -1) {
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
                            avatar = user?.avatar,
                            bubbleTextUiState = BubbleTextUiState(
                                text = value.text,
                                isMe = myId == user?.id,
                                editModeState = editModeState,
                                selected = false, //TODO: 由entity得到或者过滤器得到
                                multiSelectModeState = multiSelectMode
                            )
                        )
                    }
                }
                .catch { updateUiMessage("获取分页数据失败：${it.message}") }
                .cachedIn(viewModelScope)
        }
    }
}

data class ChatMessageUiState(
    val avatar: Uri?,
    private val entity: ChatMessageEntity,
    val bubbleTextUiState: BubbleTextUiState,
) {
    val status: MessageStatus
        get() = entity.status
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
    private val flowFlowPagingData: Flow<Flow<PagingData<ChatMessageUiState>>>,
    val message: String = "",
    val myId: Int = HUMAN_UID,
    private val topBarUiStateState: State<ChatScreenTopBarUiState>,
    private val bottomBarUiStateState: State<ChatScreenBottomBarUiState>
) {
    val topBarUiState: ChatScreenTopBarUiState
        get() = topBarUiStateState.value
    val bottomBarUiState: ChatScreenBottomBarUiState
        get() = bottomBarUiStateState.value

    @Composable
    fun lazyPagingDataItems() = flowFlowPagingData
        .collectAsState(initial = flowOf(PagingData.empty())).value
        .collectAsLazyPagingItems()
}


private const val TAG = "ChatViewModel"
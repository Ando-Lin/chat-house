@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ando.chathouse.ui.screen.state

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import com.ando.chathouse.MainScreenDestination
import com.ando.chathouse.constant.MY_UID
import com.ando.chathouse.constant.PreferencesKey
import com.ando.chathouse.data.repo.ChatRepo
import com.ando.chathouse.data.repo.UserRepo
import com.ando.chathouse.domain.entity.ChatEntity
import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.ChatContext
import com.ando.chathouse.domain.pojo.ChatMessage
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.ext.toEntity
import com.ando.chathouse.ext.toUser
import com.ando.chathouse.model.ChatModelManger
import com.ando.chathouse.profile
import com.ando.chathouse.strategy.CarryMessageStrategyManager
import com.ando.chathouse.strategy.impl.NoCarryMessageStrategy
import com.ando.chathouse.ui.component.BubbleTextUiState
import com.ando.chathouse.ui.component.exclusive.ChatScreenBottomBarUiState
import com.ando.chathouse.ui.component.exclusive.ChatScreenSettingsUiState
import com.ando.chathouse.ui.component.exclusive.ChatScreenTopBarUiState
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
    private val messageStrategyManager: CarryMessageStrategyManager,
    private val savedStateHandle: SavedStateHandle,
    private val external: CoroutineScope
) : ViewModel() {
    private val myId = MY_UID

    //从配置读取当前模型
    private val currentModelFlow: StateFlow<String> =
        context.profile.data.map { it[PreferencesKey.currentModel] ?: "" }
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    //从配置读取当前chatId
    private val currentChatIdFlow: StateFlow<Int?> =
        context.profile.data.map { it[PreferencesKey.currentChatId] }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    //当前策略
    private val currentStrategyFlow: MutableStateFlow<String> =
        MutableStateFlow(NoCarryMessageStrategy.NAME)

    //我的user实例
    private val myUserFlow = userRepo.fetchById(myId)
        .map { it?.toUser() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, User.emptyUser)


    //从savedStateHandle和配置中读取当前chatId. 以savedStateHandle的值为主
    private val latestChatIdFlow: StateFlow<Int?> = savedStateHandle
        .getStateFlow(MainScreenDestination.tabParas, null as String?)
        .onEach { Log.i(TAG, "stateflow.value=$it ") }
        .combine(currentChatIdFlow) { v1, v2 ->
            Log.i(TAG, "flow bine: v1=$v1, v2=$v2")
            when {
                v1?.isDigitsOnly() ?: false -> {
                    val id = v1!!.toIntOrNull()
                    if (id != v2) {
                        updateCurrentChatId(id)
                    }
                    id
                }
                else -> v2
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val latestChatFlow: StateFlow<ChatEntity?> =
        latestChatIdFlow
            .filter {
                it != null
            }
            .transformLatest { uid ->
                val flow = chatRepo.fetchChatById(uid!!)
                    .transform {
                        if (it == null) {
                            val chat = ChatEntity.individual(uid)
                            chatRepo.saveChat(chat)
                                .onSuccess { emit(chat) }
                                .onFailure { updateUiMessage("创建对话失败: ${it.localizedMessage}") }
                        } else {
                            emit(it)
                        }
                    }
                emitAll(flow)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                null
            )


    private val _availableModels = chatRepo.availableModelList

    private var titleFlow = latestChatFlow
        .filter { it != null }
        .flatMapLatest { userRepo.fetchById(it!!.uid) }
        .map { it?.name ?: "" }


    private val editModeState = mutableStateOf(false)
    private val multiSelectMode = mutableStateOf(false)

    private val settingsUiState = mutableStateOf(
        ChatScreenSettingsUiState(
            modelListFlow = flowOf(_availableModels),
            strategyListFlow = flowOf(messageStrategyManager.strategyList),
            currentModelFlow = currentModelFlow,
            currentStrategyFlow = currentStrategyFlow,
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
    private var selectedMessageIds: MutableSet<Int> = mutableSetOf()


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

    fun resetMessage() {
        screenUiState = screenUiState.copy(message = "")
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
        bottomBarUiState.value = bottomBarUiState.value.copy(text = { value })
    }

    /**
     * 更新消息内容
     */
    fun updateMessageContent(id: Int, content: String) {
        viewModelScope.launch {
            chatRepo.updateMessage(id = id, msg = content)
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
        val chat = latestChatFlow.value
        chat?.let {
            viewModelScope.launch {
                chatRepo.saveChat(it.copy(messageStrategy = strategy))
                    .onFailure { updateUiMessage("更新策略失败：${it.localizedMessage}") }
            }
        }
    }

    /**
     * 清除对话
     */
    fun clearConversation() {
        //获取当前chatId
        val chat = latestChatFlow.value
        chat?.let { value ->
            viewModelScope.launch {
                chatRepo.clearAllMessageByChatId(value.id)
                    .onFailure { updateUiMessage("清除对话失败：$it") }
            }
        }
    }

    /**
     * 将选中的消息id在发送时携带
     */
    fun selectedToCarry() {
        viewModelScope.launch {
            val typedArray = selectedMessageIds.toIntArray()
            chatRepo.unifyMessage(*typedArray, selected = 1)
                .onFailure { updateUiMessage("更新选中的消息时发生错误: ${it.localizedMessage}") }
        }
        selectedMessageIds = mutableSetOf()
    }

    /**
     * 将选中的消息id在发送时排除
     */
    fun selectedToExclude() {
        viewModelScope.launch {
            val typedArray = selectedMessageIds.toIntArray()
            chatRepo.unifyMessage(*typedArray, selected = -1)
                .onFailure { updateUiMessage("更新选中的消息时发生错误: ${it.localizedMessage}") }
        }
        selectedMessageIds = mutableSetOf()
    }

    /**
     * 将选中的消息id删除
     */
    fun selectedTODelete() {
        viewModelScope.launch {
            selectedMessageIds.forEach {
                chatRepo.deleteMessage(it)
                    .onFailure { updateUiMessage("删除选中的消息时发生错误: ${it.localizedMessage}") }
            }
        }
        selectedMessageIds = mutableSetOf()
    }

    /**
     * 收集选中的消息id
     */
    fun collectSelectedId(messageId: Int) {
        if (multiSelectMode.value) {
            selectedMessageIds.add(messageId)
        }
    }

    /**
     * 转变多选模式
     */
    fun switchMultiSelectModeState(state: Boolean? = null) {
        val targetState = state ?: !(settingsUiState.value.multiSelectMode)
        multiSelectMode.value = targetState
        selectedMessageIds = mutableSetOf()
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
        val chat = latestChatFlow.value
        if (chat == null) {
            updateUiMessage("角色不存在")
            return
        }

        //计算当前时间与上个消息的时间差
        val now = LocalDateTime.now()
        val last = previousMsgTime ?: LocalDateTime.MIN
        val secondDiff = Duration.between(last, now).seconds

        //创建消息实例
        val message = ChatMessage(
            chatId = chat.id,
            uid = if (fromMe) myId else chat.uid,
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
                chatRepo.saveMessage(message.toEntity())
                    .onFailure {
                        updateUiMessage("保存消息异常：${it.message}")
                        Log.e(TAG, "sendMessage: ", it)
                    }
            }
        } else {

            //消息发送
            viewModelScope.launch {
                //获取当前模型
                val currentModel = currentModelFlow.first()
                chatRepo.sendMessage(message = message, modelName = currentModel)
                    .onFailure {
                        Log.e(TAG, "sendMessage: ", it)
                        updateUiMessage("消息发送失败：${it.message}")
                    }
            }
        }

        //重置输入框文本
        updateInputText("")
    }

    /**
     * 重发送消息
     */
    fun resendMessage(msgId: Int) {
        viewModelScope.launch {
            val model = currentModelFlow.value
            chatRepo.resendMessage(model, msgId)
                .onFailure { updateUiMessage("重发消息失败：$it.l") }
        }
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
        return latestChatFlow
            .filter { it != null }
            .mapLatest { chat ->
                this.currentStrategyFlow.value = chat!!.messageStrategy
                val user = withContext(viewModelScope.coroutineContext) {
                    userRepo.fetchById(chat.uid).first()
                }
                val chatContext = ChatContext(myUid = myId)
                Pager(config = PagingConfig(pageSize = 25)) {
                    chatRepo.getMessagePagingSourceByChatId(chat.id)
                }
                    .flow
                    .map { pagingData ->
                        val filter = messageStrategyManager.filterBy(chat.messageStrategy)
                        pagingData
                            .map { value ->
                                val avatar =
                                    if (value.uid == user?.id) user.avatar
                                    else myUserFlow.value?.avatar
                                val selected =
                                    when (value.selected) {
                                        1 -> true
                                        -1 -> false
                                        else -> filter(value, chatContext)
                                    }
                                ChatMessageUiState(
                                    entity = value,
                                    avatar = avatar,
                                    bubbleTextUiState = BubbleTextUiState(
                                        text = { value.text },
                                        isMe = myId == value.uid,
                                        editModeState = editModeState,
                                        selected = selected,
                                        reading = value.status == MessageStatus.Reading,
                                        multiSelectModeState = multiSelectMode
                                    )
                                )
                            }
                    }
                    .catch {
                        Log.e(TAG, "getPagingData: ", it)
                        updateUiMessage("获取分页数据失败：${it.message}")
                    }
                    .cachedIn(viewModelScope)
            }.catch {
                Log.e(TAG, "getPagingData: ", it)
                updateUiMessage("获取分页数据失败：${it.message}")
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
    val myId: Int = MY_UID,
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
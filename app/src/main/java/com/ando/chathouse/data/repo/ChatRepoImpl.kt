package com.ando.chathouse.data.repo

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.paging.PagingSource
import com.ando.chathouse.constant.*
import com.ando.chathouse.data.source.local.ChatLocalDataSource
import com.ando.chathouse.di.IoDispatcher
import com.ando.chathouse.domain.entity.ChatEntity
import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.ChatContext
import com.ando.chathouse.domain.pojo.ChatMessage
import com.ando.chathouse.domain.pojo.PageQuery
import com.ando.chathouse.domain.pojo.RoleMessage
import com.ando.chathouse.exception.HttpRequestException
import com.ando.chathouse.exception.MessageStreamInterruptException
import com.ando.chathouse.exception.NoSuchChatException
import com.ando.chathouse.exception.NoSuchUserException
import com.ando.chathouse.ext.relativeToNowSecondDiff
import com.ando.chathouse.ext.toEntity
import com.ando.chathouse.model.ChatModel
import com.ando.chathouse.model.ChatModelManger
import com.ando.chathouse.profile
import com.ando.chathouse.strategy.CarryMessageStrategyManager
import com.ando.chathouse.util.TokenUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.LocalDateTime
import javax.inject.Inject


class ChatRepoImpl @Inject constructor(
    private val localDS: ChatLocalDataSource,
    private val chatModelManger: ChatModelManger,
    private val messageStrategyManager: CarryMessageStrategyManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val externalScope: CoroutineScope,
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context,
) : ChatRepo {

    override val availableModelList: List<String>
        get() = chatModelManger.models.map { it.key }

    init {
        //转换所有发送状态为失败状态
        externalScope.launch {
            localDS.shiftStatus(
                originStatus = MessageStatus.Sending,
                targetStatus = MessageStatus.Failed
            )
        }
        //转换所有读取状态为中断状态
        externalScope.launch {
            localDS.shiftStatus(
                originStatus = MessageStatus.Reading,
                targetStatus = MessageStatus.Interrupt
            )
        }
    }

    private fun getInstructionRoleMessages(
        roleName: String,
        roleDescription: String,
        userName: String = ""
    ): List<RoleMessage> {
        val formattedName = if (roleName.isNotBlank()) "角色名称:$roleName," else ""
        val formattedDescription = "角色描述:$roleDescription"
        val formattedUserName = if (userName.isNotBlank()) "你的对面角色叫\"$userName\"." else ""
        return listOf(
            RoleMessage(
                role = RoleMessage.SYSTEM_ROLE,
                content = "请你根据你之前的回答扮演角色."
            ),
            RoleMessage(
                role = RoleMessage.SYSTEM_ROLE,
                content = "你正在扮演角色，${formattedUserName}接下来是你的角色的描述，请以角色口吻回答并描述神情和体态在括号内,根据角色的年龄和描述调整语言"
            ),
            RoleMessage(
                role = RoleMessage.SYSTEM_ROLE,
                content = "$formattedName$formattedDescription"
            )
        )
    }

    override fun getRecentChatPagingSource(): PagingSource<Int, ChatMessageEntity> {
        return localDS.getRecentChatPagingSource()
    }

    override fun getMessagePagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity> {
        return localDS.getMessagePagingSourceByChatId(chatId = chatId)
    }

    override fun getLatestMessageByChatId(chatId: Int): Flow<ChatMessageEntity?> {
        return localDS.getLatestMessageByChatId(chatId = chatId)
    }

    override fun getPagingByCid(
        chatId: Int,
        uid: Int?,
        pageQuery: PageQuery
    ): Flow<List<ChatMessageEntity>> {
        return localDS.getMessagePagingByCid(chatId = chatId, uid = uid, pageQuery = pageQuery)
    }

    /**
     * 获取用户的历史记录消息。只针对中文字符做处理。TODO: 统计token而非字符数
     * 以消息时间降序遍历，当达到或超过最大字符数时或者达到最大页数时将停止添加消息
     * @param chatId: 对话id
     * @param strategy: 过滤或者选择携带消息的策略
     * @return: 角色消息列表。
     */
    private suspend fun getHistory(
        chatId: Int,
        strategy: String,
        isNotReachCeil: (RoleMessage)->Boolean
    ): MutableList<RoleMessage> {
        val pageSize = 20
        val list = mutableListOf<RoleMessage>()

        //用于过滤的上下文
        val chatContext = ChatContext(
            myUid = MY_UID,
        )

        val filter = messageStrategyManager.filterBy(strategy)

        val pageFlow = fetchAllMessageFlowByChatId(pageSize = pageSize, chatId = chatId)

        pageFlow
            .filter {
                when (it.selected) {
                    1 -> true
                    -1 -> false
                    else -> filter(it, chatContext)
                }
            }
            .map {
                when(it.uid){
                    MY_UID -> RoleMessage.userMessage(it.text)
                    else -> RoleMessage.assistantMessage(it.text)
                }
            }
            .takeWhile { isNotReachCeil(it) }
            .onEach { list.add(0, it) }
            .collect()


        return list
    }

    /**
     * 获取chatId的所有消息。
     * 原理：当下游收集pageSize大小的数据后自动发送下一页的flow
     */
    private fun fetchAllMessageFlowByChatId(pageSize: Int, chatId: Int): Flow<ChatMessageEntity> {
        return flow {
            var page = 1
            var realSize: Int = pageSize
            while (pageSize == realSize) {
                val flow = getPagingByCid(
                    chatId = chatId,
                    pageQuery = PageQuery(pageSize = pageSize, page = page++)
                )
                    .onEach { realSize = it.size }
                    .onCompletion {
                        Log.i(TAG, "getHistory: flow onCompletion")
                    }
                val result = flow.first()
                emitAll(result.asFlow())
            }
        }
    }

    private suspend fun getApiKey(modelName: String): String? {
        val label = chatModelManger.models[modelName]?.value?.label
        label ?: return null
        val key = stringPreferencesKey(label + PreferencesKey.apiKeySuffix)
        return context.profile.data.map { it[key] }.firstOrNull()
    }

    /**
     * 发送消息。
     */
    override suspend fun sendMessage(modelName: String, message: ChatMessage): Result<Int> {
        Log.i(TAG, "sendMessage: \n modelName=$modelName \n message=$message")

        return withContext(externalScope.coroutineContext + ioDispatcher) {
            kotlin.runCatching {

                val chatId = message.chatId
                val chat = withContext(ioDispatcher) {
                    fetchChatById(chatId).first()
                }
                chat ?: throw NoSuchChatException(chatId)

                //将待发送的消息插入数据库
                val messageId = localDS.insertMessage(message.toEntity())

                try {
                    val result = continueSendMessage(modelName = modelName, chatId = chatId, messageContent = message.text){
                        //查询成功则更新发送状态
                        withContext(ioDispatcher) {
                            updateMessage(id = messageId, status = MessageStatus.Success)
                        }
                    }
                    return@runCatching result.getOrThrow()
                } catch (e: MessageStreamInterruptException) {
                    Log.e(TAG, "sendMessage: 接收消息异常", e)
                    throw e
                } catch (e: HttpException) {
                    updateMessage(id = messageId, status = MessageStatus.Failed)
                    Log.e(TAG, "sendMessage: 网络异常", e)
                    throw HttpRequestException(e)
                } catch (e: Exception) {
                    updateMessage(id = messageId, status = MessageStatus.Failed)
                    Log.e(TAG, "sendMessage: 发送异常", e)
                    throw e
                }


            }
        }
    }

    override suspend fun resendMessage(modelName: String, messageId: Int): Result<Int> {
        return withContext(externalScope.coroutineContext + ioDispatcher) {
            kotlin.runCatching {
                val chatMessage = withContext(ioDispatcher) {
                    localDS.getMessageById(messageId).first()
                }
                chatMessage ?: throw IllegalArgumentException("消息不存在：$messageId")

                val chatId = chatMessage.chatId

                val chat = withContext(ioDispatcher) {
                    fetchChatById(chatId).first()
                }
                chat ?: throw NoSuchChatException(chatId)

                //转变为发送状态
                updateMessage(id = messageId, status = MessageStatus.Sending)


                try {
                    val result = continueSendMessage(modelName = modelName, chatId = chatId, messageContent = chatMessage.text){
                        //创建重发的消息
                        val latestMessage = withContext(ioDispatcher) {
                            localDS.getLatestMessageByChatId(chatId = chatId).first()
                        }
                        val (timestamp, secondDiff) = latestMessage?.timestamp.relativeToNowSecondDiff()
                        val resentMessage = chatMessage.copy(
                            id = 0,
                            secondDiff = secondDiff,
                            timestamp = timestamp,
                            status = MessageStatus.Success
                        )
                        //删除旧消息
                        withContext(ioDispatcher) {
                            localDS.deleteMessage(messageId)
                        }
                        //插入已重发的消息
                        localDS.insertMessage(resentMessage)
                    }
                    return@runCatching result.getOrThrow()
                } catch (e: MessageStreamInterruptException) {
                    Log.e(TAG, "resendMessage: 接收消息异常", e)
                    throw e
                } catch (e: HttpException) {
                    updateMessage(id = messageId, status = MessageStatus.Failed)
                    Log.e(TAG, "resendMessage: 网络异常", e)
                    throw HttpRequestException(e)
                } catch (e: Exception) {
                    updateMessage(id = messageId, status = MessageStatus.Failed)
                    Log.e(TAG, "resendMessage: 发送异常", e)
                    throw e
                }
            }
        }
    }

    override suspend fun continueSendMessage(
        modelName: String,
        chatId: Int,
        messageContent: String?,
        onSendSuccess: (suspend () -> Unit)?
    ): Result<Int> {
        return withContext(externalScope.coroutineContext){
            kotlin.runCatching {
                val chat = fetchChatById(chatId).first() ?: throw NoSuchChatException(chatId)
                //组装上下文和待发送的信息
                val roleMessages =
                    composeContextForSendMessage(chatId = chatId, message = messageContent)

                //发送消息
                val messageFlow = sendMessageRequest(modelName, roleMessages)

                //手动设置id
                val msgId = System.currentTimeMillis().toInt()

                //接收成功时
                val onReceive:suspend (String?)->Unit = {firstMsg->
                    //请求成功则执行回调
                    onSendSuccess?.invoke()
                    //计算时间差
                    val messageEntity = withContext(ioDispatcher) {
                        localDS.getLatestMessageByChatId(chatId = chatId).first()
                    }
                    val (now, diff) = messageEntity?.timestamp.relativeToNowSecondDiff()
                    //插入到本地数据库中
                    localDS.insertMessage(
                        ChatMessageEntity(
                            id = msgId,
                            chatId = chatId,
                            uid = chat.uid,
                            text = firstMsg ?: "",
                            timestamp = now,
                            secondDiff = diff,
                            status = MessageStatus.Reading
                        )
                    )
                }

                //流式收集信息
                streamMessage(messageId = msgId, flow = messageFlow){
                    launch {
                        onReceive(it)
                    }
                }

                return@runCatching msgId
            }
        }
    }

    /**
     * 流式收集消息
     */
    private suspend fun streamMessage(messageId: Int, flow: Flow<String?>, onReceive: suspend (String?)->Unit = {}) {
        val stringBuilder = StringBuilder()
        //token增量
        var tokenDelta = 0
        //时间增量
        var timeDelta: Long
        //上次操作时间
        var lastTimeMillis = System.currentTimeMillis()
        var first = true
        withContext(ioDispatcher) {
            flow
                .onCompletion { throwable ->
                    //确保全部写入
                    updateMessage(messageId, stringBuilder.toString())
                    //根据是否有异常写入状态
                    val status = when (throwable == null) {
                        true -> MessageStatus.Success
                        else -> MessageStatus.Interrupt
                    }
                    updateMessage(messageId, status = status)
                }
                .collect {
                    //接收第一条消息
                    if (first){
                        onReceive(it)
                        first = false
                    }

                    it ?: return@collect

                    stringBuilder.append(it)

                    tokenDelta++

                    val nowMillis = System.currentTimeMillis()

                    timeDelta = nowMillis - lastTimeMillis

                    //增量超过阈值时写入数据库
                    if (timeDelta > WRITE_DB_TIME_THRESHOLD || tokenDelta > WRITE_DB_TOKEN_THRESHOLD) {
                        updateMessage(messageId, stringBuilder.toString())
                        lastTimeMillis = nowMillis
                        tokenDelta = 0
                    }

                }
        }

    }

    private fun getLastMessageTimeForResend(
        resendMessage: ChatMessageEntity,
        twoMessage: List<ChatMessageEntity>
    ): LocalDateTime? {
        var lastMessageTime: LocalDateTime? = null
        twoMessage.forEach {
            if (it != resendMessage) {
                lastMessageTime = it.timestamp
                return@forEach
            }
        }
        return lastMessageTime
    }

    override suspend fun unifyMessage(vararg id: Int, selected: Int?): Result<Unit> {
        return withContext(externalScope.coroutineContext) {
            kotlin.runCatching {
                localDS.unifyMessage(*id, selected = selected)
            }
        }
    }

    override fun fetchChatById(id: Int): Flow<ChatEntity?> {
        return localDS.loadChatById(id).flowOn(ioDispatcher)
    }

    override suspend fun saveChat(chatEntity: ChatEntity): Result<Int> {
        return withContext(externalScope.coroutineContext + ioDispatcher) {
            kotlin.runCatching {
                localDS.saveChat(chatEntity)
            }
        }
    }

    override suspend fun deleteChat(id: Int): Result<Unit> {
        return withContext(externalScope.coroutineContext + ioDispatcher) {
            kotlin.runCatching {
                localDS.deleteChat(id)
            }
        }
    }

    override suspend fun clearAllMessageByChatId(chatId: Int): Result<Unit> {
        return withContext(externalScope.coroutineContext) {
            kotlin.runCatching {
                localDS.clearChatMessage(chatId)
            }
        }
    }

    private suspend fun composeContextForSendMessage(
        chatId: Int,
        message: String?
    ): List<RoleMessage> {
        //token计数
        var tokenCounter = 0

        //获取chat
        val chat = withContext(ioDispatcher) {
            localDS.loadChatById(chatId).first()
        }
        chat ?: throw NoSuchChatException(chatId)

        //获取chatId的角色
        val role = withContext(ioDispatcher) {
            userRepo.fetchById(chat.uid).first() ?: throw NoSuchUserException(chatId)
        }

        //获取用户本人
        val me = withContext(ioDispatcher) {
            userRepo.fetchById(MY_UID).first()!!
        }

        //获取策略名称
        val strategyName = chat.messageStrategy

        //预置的消息
        val prependMessages = when(role.enableGuide){
            true -> {
                getInstructionRoleMessages(
                    roleName = role.name,
                    roleDescription = role.description,
                    userName = me.name
                )
            }
            else -> listOf(RoleMessage.systemMessage(role.description))
        }
        //带发送的消息
        val latestMessage = message?.let { RoleMessage.userMessage(message) }
        //用于提醒的消息
        val reminderMessage = RoleMessage.systemMessage(role.reminder)


        tokenCounter += TokenUtils.computeToken(prependMessages)
        tokenCounter += TokenUtils.computeToken(latestMessage)
        tokenCounter += TokenUtils.computeToken(reminderMessage.takeIf { role.enableReminder })

        //获取上下文
        val roleMessages = withContext(ioDispatcher) {
            getHistory(chatId = chatId, strategy = strategyName){
                //当token剩余量仍大于预留量时返回true，否则返回false
                tokenCounter += TokenUtils.computeToken(it)
                return@getHistory (MAX_TOKEN - tokenCounter) > RESERVED_TOKEN
            }
        }

        //添加预置指令
        roleMessages.addAll(index = 0, elements = prependMessages)


        //添加最新消息
        latestMessage?.let {
            roleMessages.add(
                roleMessages.size,
                it
            )
        }

        //添加提醒
        if (role.enableReminder) {
            roleMessages.add(
                roleMessages.size,
                reminderMessage
            )
        }


        Log.i(TAG, "composeContextForSendMessage: \n roleMessages = $roleMessages")
        Log.i(TAG, "composeContextForSendMessage: tokenCounter = $tokenCounter")

        return roleMessages
    }

    private suspend fun sendMessageRequest(
        modelName: String,
        roleMessages: List<RoleMessage>,
    ): Flow<String?> {
        //从配置文件中获取apiKey
        val apiKey = getApiKey(modelName)
        //实际发送消息
        return chatModelManger.sendMessages(
            modelName = modelName,
            para = ChatModel.Para(apiKey = apiKey),
            messages = roleMessages
        )
    }

    override suspend fun saveMessage(messageEntity: ChatMessageEntity): Result<Int> {
        return withContext(externalScope.coroutineContext) {
            kotlin.runCatching {
                localDS.insertMessage(messageEntity)
            }
        }
    }

    override suspend fun deleteMessage(id: Int): Result<Unit> {
        return withContext(externalScope.coroutineContext) {
            kotlin.runCatching {
                localDS.deleteMessage(id)
            }
        }
    }

    override suspend fun updateMessage(
        id: Int,
        msg: String?,
        status: MessageStatus?
    ): Result<Unit> {
        return withContext(externalScope.coroutineContext) {
            kotlin.runCatching {
                localDS.updateMessage(id, status, msg)
            }
        }
    }

}

private const val TAG = "ChatRepoImpl"
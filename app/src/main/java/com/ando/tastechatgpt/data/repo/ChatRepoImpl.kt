package com.ando.tastechatgpt.data.repo

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.paging.PagingSource
import com.ando.tastechatgpt.constant.MY_UID
import com.ando.tastechatgpt.constant.PreferencesKey
import com.ando.tastechatgpt.data.source.local.ChatLocalDataSource
import com.ando.tastechatgpt.di.IoDispatcher
import com.ando.tastechatgpt.domain.entity.ChatEntity
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.*
import com.ando.tastechatgpt.exception.HttpRequestException
import com.ando.tastechatgpt.exception.NoSuchChatException
import com.ando.tastechatgpt.exception.NoSuchUserException
import com.ando.tastechatgpt.ext.relativeToNowSecondDiff
import com.ando.tastechatgpt.model.ChatModel
import com.ando.tastechatgpt.model.ChatModelManger
import com.ando.tastechatgpt.profile
import com.ando.tastechatgpt.strategy.CarryMessageStrategyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.time.Duration
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

    private fun getInstructionRoleMessages(name: String, description: String): List<RoleMessage> {
        val formattedName = if (name.isNotBlank()) "角色名称:$name," else ""
        val formattedDescription = "角色描述:" + description.ifBlank { "智能助手" }
        return listOf(
            RoleMessage(
                role = RoleMessage.SYSTEM_ROLE,
                content = "请你根据你之前的回答接下去对话(若缺少信息则推理情境)"
            ),
            RoleMessage(
                role = RoleMessage.SYSTEM_ROLE,
                content = "你正在扮演角色，接下来是这个角色的描述，请以该角色的口吻进行回答并在括号描述神情和体态"
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
     * 获取用户的历史记录消息。会尽可能添加用户的消息和上一条ai的消息，
     * 该函数尝试以小分页的方式一边记录已添加的消息超过最大令牌数一边添加消息，当达到或超过最大令牌数时或者达到最大页数时将停止添加消息
     * 注意：该函数将ai消息放在最后
     * @param chatId: 对话id
     * @param strategy: 过滤或者选择携带消息的策略
     * @return: 角色消息列表。
     */
    private suspend fun getHistory(
        chatId: Int,
        strategy: String
    ): MutableList<RoleMessage> {
        val pageSize = 20
        val list = mutableListOf<RoleMessage>()
        //600是指令token的估计值，0.87是中文字符/token的大致比率
        val maxToken: Int = (4096 * 0.87).toInt() - 600
        //字符计数
        var count = 0
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
                RoleMessage(role = RoleMessage.USER_ROLE, content = it.text)
            }
            .onEach { value -> count += value.content.length }
            .takeWhile { count < maxToken }
            .onEach { list.add(0, it) }
            .onCompletion {
                Log.i(TAG, "getHistory: flow onCompletion")
            }
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
                    //组装上下文和待发送的信息
                    val roleMessages =
                        composeContextForSendMessage(chatId = chatId, message = message.text)

                    //发送消息
                    val messageFlow = sendMessage(modelName, roleMessages)

                    val firstMessage = withContext(ioDispatcher) {
                        messageFlow.first()
                    }
                    //查询成功则更新发送状态
                    withContext(ioDispatcher) {
                        updateMessage(id = messageId, status = MessageStatus.Success)
                    }
                    //计算时间差
                    val messageEntity = withContext(ioDispatcher) {
                        localDS.getLatestMessageByChatId(chatId = chatId).first()
                    }
                    val (now, diff) = messageEntity?.timestamp.relativeToNowSecondDiff()
                    //插入到本地数据库中
                    val msgId = localDS.insertMessage(
                        ChatMessageEntity(
                            chatId = chatId,
                            uid = chat.uid,
                            text = firstMessage ?: "",
                            timestamp = now,
                            secondDiff = diff,
                            status = MessageStatus.Reading
                        )
                    )
                    //流式收集信息
                    streamMessage(messageId = msgId, flow = messageFlow)

                    return@runCatching msgId
                }catch (e: HttpException) {
                    updateMessage(id = messageId, status = MessageStatus.Failed)
                    Log.e(TAG, "sendMessage: 网络异常", e)
                    throw HttpRequestException(e)
                } catch (e: Exception) {
                    updateMessage(id = messageId, status = MessageStatus.Failed)
                    Log.e(TAG, "sendMessage: 网络异常", e)
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
                    //组装数据
                    val roleMessages = withContext(ioDispatcher) {
                        composeContextForSendMessage(
                            chatId = chatMessage.chatId,
                            message = chatMessage.text
                        )
                    }

                    val messageFlow =
                        sendMessage(modelName = modelName, roleMessages = roleMessages)

                    //第一个值阻塞收集
                    val firstMessage = withContext(ioDispatcher) {
                        messageFlow.first() ?: ""
                    }

                    //收集最新的两个消息
                    val latestTwoMessage = withContext(externalScope.coroutineContext) {
                        getPagingByCid(
                            chatId = chatMessage.chatId,
                            pageQuery = PageQuery(pageSize = 2)
                        ).first()
                    }

                    //创建重发的消息
                    val lastMessageTime = getLastMessageTimeForResend(chatMessage, latestTwoMessage)
                    val (timestamp, secondDiff) = lastMessageTime.relativeToNowSecondDiff()
                    val resentMessage = chatMessage.copy(
                        id = 0,
                        secondDiff = secondDiff,
                        timestamp = timestamp,
                        status = MessageStatus.Success
                    )

                    //删除
                    withContext(ioDispatcher) {
                        localDS.deleteMessage(messageId)
                    }


                    val msgId = withContext(ioDispatcher) {
                        //插入已重发的消息
                        localDS.insertMessage(resentMessage)
                        delay(700)
                        //插入第一条响应的消息
                        val messageCreatTime = LocalDateTime.now()
                        localDS.insertMessage(
                            ChatMessageEntity(
                                chatId = chatId,
                                uid = chat.uid,
                                text = firstMessage,
                                timestamp = messageCreatTime,
                                secondDiff = Duration.between(timestamp, messageCreatTime).seconds,
                                status = MessageStatus.Reading
                            )
                        )
                    }

                    streamMessage(messageId = msgId, flow = messageFlow)

                    return@runCatching msgId
                } catch (e: Exception) {
                    updateMessage(id = messageId, status = MessageStatus.Failed)
                    Log.e(TAG, "resendMessage: 发送消息异常", e)
                    throw e
                }
            }
        }
    }

    /**
     * 流式收集消息
     */
    private suspend fun streamMessage(messageId: Int, flow: Flow<String?>) {
        val stringBuilder = StringBuilder()
        //字符增量
        var charDelta = 0
        //增量阈值
        val deltaThreshold = 20
        //时间增量
        var timeDelta: Long
        //上次操作时间
        var lastTimeMillis = System.currentTimeMillis()
        //时间阈值
        val timeThreshold = 500
        withContext(ioDispatcher){
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
                    it ?: return@collect

                    stringBuilder.append(it)

                    charDelta += it.length

                    val nowMillis = System.currentTimeMillis()

                    timeDelta = nowMillis - lastTimeMillis

                    //增量超过阈值时写入数据库
                    if (timeDelta > timeThreshold || charDelta > deltaThreshold) {
                        updateMessage(messageId, stringBuilder.toString())
                        lastTimeMillis = nowMillis
                        charDelta = 0
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
        message: String
    ): List<RoleMessage> {

        //获取chat
        val chat = withContext(ioDispatcher) {
            localDS.loadChatById(chatId).first()
        }
        chat ?: throw NoSuchChatException(chatId)

        //获取chatId的用户
        val user = withContext(ioDispatcher) {
            userRepo.fetchById(chat.uid).first() ?: throw NoSuchUserException(chatId)
        }

        //获取策略名称
        val strategyName = chat.messageStrategy

        //获取上下文
        val roleMessages = withContext(ioDispatcher) {
            getHistory(chatId = chatId, strategy = strategyName)
        }

        //添加指令
        if (user.enableGuide) {
            roleMessages.addAll(
                0,
                getInstructionRoleMessages(
                    name = user.name,
                    description = user.description
                )
            )
        } else {
            roleMessages.add(
                0,
                RoleMessage.systemMessage(user.description)
            )
        }

        Log.i(TAG, "sendMessage: \n roleMessages = $roleMessages")


        //添加提醒
        if (user.enableReminder) {
            roleMessages.add(
                roleMessages.size,
                RoleMessage.systemMessage(user.reminder)
            )
        }

        //添加最新消息
        roleMessages.add(
            roleMessages.size,
            RoleMessage.userMessage(message)
        )

        return roleMessages
    }

    private suspend fun sendMessage(
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
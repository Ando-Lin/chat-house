package com.ando.tastechatgpt.data.repo

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.paging.PagingSource
import com.ando.tastechatgpt.constant.HUMAN_UID
import com.ando.tastechatgpt.constant.PreferencesKey
import com.ando.tastechatgpt.data.source.local.ChatLocalDataSource
import com.ando.tastechatgpt.di.IoDispatcher
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.ChatMessage
import com.ando.tastechatgpt.domain.pojo.PageQuery
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import com.ando.tastechatgpt.domain.pojo.toEntity
import com.ando.tastechatgpt.exception.FetchDataException
import com.ando.tastechatgpt.exception.HttpRequestException
import com.ando.tastechatgpt.exception.NoSuchUserException
import com.ando.tastechatgpt.model.ChatModel
import com.ando.tastechatgpt.model.ChatModelManger
import com.ando.tastechatgpt.profile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class ChatRepoImpl @Inject constructor(
    private val localDS: ChatLocalDataSource,
    private val chatModelManger: ChatModelManger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val externalScope: CoroutineScope,
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context,
) : ChatRepo {

    override val availableModelList: List<String>
        get() = chatModelManger.models.map {
            Log.i(TAG, "chatModelManger.models.map: $it")
            it.name
        }.toList()

    init {
        //转换所有发送状态为失败状态
        externalScope.launch {
            localDS.shiftStatus(
                originStatus = MessageStatus.Sending,
                targetStatus = MessageStatus.Failed
            )
        }
    }

    private fun getInstructionRoleMessages(name: String, description: String): List<RoleMessage> {
        val formattedName = if (name.isNotBlank()) "名称:$name," else ""
        val formattedDescription = "描述:" + description.ifBlank { "智能助手" }
        return listOf(
            RoleMessage(
                role = RoleMessage.SYSTEM_ROLE,
                content = "你收到信息中角色的回答被剔除到只剩下最后一条，你应该根据user的信息推断中间的对话。"
            ),
            RoleMessage(
                role = RoleMessage.SYSTEM_ROLE,
                content = "你正在排练角色，接下来是这个角色的描述，请以该角色的口吻进行回答"
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

    override fun getPagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity> {
        return localDS.getPagingSourceByChatId(chatId = chatId)
    }

    override fun getLatestByChatId(chatId: Int): Flow<ChatMessageEntity?> {
        return localDS.getLatestByChatId(chatId = chatId)
    }

    override fun getPagingByCidAndUid(
        chatId: Int,
        uid: Int,
        pageQuery: PageQuery
    ): Flow<List<ChatMessageEntity>> {
        return localDS.getPagingByCidAndUid(chatId = chatId, uid = uid, pageQuery = pageQuery)
    }

    /**
     * 获取用户的历史记录消息。会尽可能添加用户的消息和上一条ai的消息，
     * 该函数尝试以小分页的方式一边记录已添加的消息超过最大令牌数一边添加消息，当达到或超过最大令牌数时或者达到最大页数时将停止添加消息
     * 注意：该函数将ai消息放在最后
     * @param chatId: 对话id
     * @param uid: 用户id
     * @return: 角色消息列表。
     */
    @OptIn(FlowPreview::class)
    private suspend fun getHistory(chatId: Int, uid: Int): MutableList<RoleMessage> {
        var page = 1
        val pageSize = 20
        val list = mutableListOf<RoleMessage>()
        //600是指令token的估计值，0.87是中文字符/token的大致比率
        val maxToken: Int = (4096 * 0.87).toInt() - 600
        val aiLastMessage = getPagingByCidAndUid(
            chatId = chatId,
            uid = uid,
            pageQuery = PageQuery(pageSize = 1, page = 1)
        ).map { if (it.isNotEmpty()) it[0] else null }
            .firstOrNull()
        var count: Int = aiLastMessage?.text?.length ?: 0


        val pageFlow = flow<ChatMessageEntity> {
            var realSize: Int = pageSize
            while (pageSize == realSize) {
                val flow = getPagingByCidAndUid(
                    chatId = chatId,
                    uid = HUMAN_UID,
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

        pageFlow
            .distinctUntilChanged()
            .filter { it.status == MessageStatus.Success }
            .map {
                RoleMessage(role = RoleMessage.USER_ROLE, content = it.text)
            }
            .onEach { value -> count += value.content.length }
            .takeWhile { count < maxToken }
            .onEach { list.add(0, it) }
            .onCompletion {
                Log.i(TAG, "getHistory: flow onCompletion")
            }
            .collect {
                Log.i(TAG, "getHistory: it=$it")
            }

//        while (true) {
//            //获取异步流
//            val flow = getPagingByCidAndUid(
//                chatId = chatId,
//                uid = HUMAN_UID,
//                pageQuery = PageQuery(pageSize = pageSize, page = page)
//            )
//            //从异步流获取历史消息
//            flow
//                .map {
//                    realSize = it.size
//                    it.asFlow()
//                }
//                .flattenConcat()
//                .filter { it.status == MessageStatus.Success }
//                .map {
//                    RoleMessage(role = RoleMessage.USER_ROLE, content = it.text)
//                }
//                .onEach { value -> count += value.content.length }
//                .onEach { list.add(0, it) }
//                .onCompletion {  }
//                .collect{
//                    Log.i(TAG, "getHistory: it=$it")
//                }
//
//            //当字数超过最大token时不再查询历史
//            if (count < maxToken && realSize == pageSize)
//                page++
//            else
//                break
//        }
        aiLastMessage?.let {
            list.add(RoleMessage(role = "assistant", content = it.text))
        }
        return list
    }

    private suspend fun getApiKey(modelName: String): String {
        val key = stringPreferencesKey(modelName + PreferencesKey.apiKeySuffix)
        return context.profile.data.firstOrNull()?.get(key) ?: ""
    }

    /**
     * 发送消息。目前chatId等于AI的uid。
     * TODO: 建立chatId和uid的表，通过查询chat来获取一个或一组uid
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun sendMessage(modelName: String, message: ChatMessage) {
        Log.i(TAG, "sendMessage: \n modelName=$modelName \n message=$message")

        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "sendMessage: 发送消息异常", throwable)
        }
        val deferred = externalScope.async(ioDispatcher + handler) {
            val chatId = message.chatId
            //获取chatId的用户
            val user =
                userRepo.fetchById(chatId).first() ?: throw NoSuchUserException(chatId)
            //获取用户发送的信息
            val roleMessages =
                getHistory(chatId = message.chatId, uid = message.chatId)
            //添加指令
            roleMessages.addAll(
                0,
                getInstructionRoleMessages(
                    name = user.name ?: "",
                    description = user.description ?: ""
                )
            )

            Log.i(TAG, "sendMessage: \n roleMessages = $roleMessages")

            //添加最新消息
            roleMessages.add(roleMessages.size, RoleMessage(RoleMessage.USER_ROLE, message.text))
            //插入数据库
            val id = localDS.insert(message.toEntity())
            try {
                //从配置文件中获取apiKey
                val apiKey = getApiKey(modelName)
                getPagingByCidAndUid(
                    chatId = message.chatId,
                    uid = message.uid,
                    pageQuery = PageQuery(pageSize = 20, page = 1)
                )
                //查询
                val responseMessage = chatModelManger.sendMessages(
                    modelName = modelName,
                    para = ChatModel.Para(apiKey = apiKey),
                    messages = roleMessages
                ).first()
                //查询成功则更新发送状态
                update(id = id, status = MessageStatus.Success)
                //计算时间差
                val flow = localDS.getLatestByChatId(chatId = chatId)
                val messageEntity = flow.first()
                val timestamp = messageEntity?.timestamp ?: LocalDateTime.MIN
                val now = LocalDateTime.now()
                val diff = Duration.between(timestamp, now).seconds
                //插入到本地数据库中
                localDS.insert(
                    ChatMessageEntity(
                        chatId = chatId,
                        uid = chatId,
                        text = responseMessage ?: "",
                        timestamp = now,
                        secondDiff = diff
                    )
                )
            } catch (e: HttpException) {
                update(id = id, status = MessageStatus.Failed)
                throw HttpRequestException(e)
            } catch (e: Exception) {
                update(id = id, status = MessageStatus.Failed)
                throw e
            }
        }
    }

    override suspend fun save(messageEntity: ChatMessageEntity): Int {
        return withContext(externalScope.coroutineContext) {
            localDS.insert(messageEntity)
        }
    }

    override fun deleteMessage(id: Int) {
        externalScope.launch {
            localDS.delete(id)
        }
    }

    override fun update(id: Int, msg: String?, status: MessageStatus?) {
        externalScope.launch {
            localDS.update(id, status, msg)
        }
    }

}

private const val TAG = "ChatRepoImpl"
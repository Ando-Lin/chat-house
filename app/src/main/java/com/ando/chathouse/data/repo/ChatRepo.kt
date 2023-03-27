package com.ando.chathouse.data.repo

import androidx.paging.PagingSource
import com.ando.chathouse.domain.entity.ChatEntity
import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.ChatMessage
import com.ando.chathouse.domain.pojo.PageQuery
import kotlinx.coroutines.flow.Flow

//TODO: 将发送消息的业务转移到worker中
interface ChatRepo {
    val availableModelList: List<String>

    fun getRecentChatPagingSource(): PagingSource<Int, ChatMessageEntity>

    /**
     * 获取聊天对象的聊天分页
     * @param chatId: 聊天对象Id
     */
    fun getMessagePagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity>

    /**
     * 获取聊天对象的最新一条消息
     * @param chatId: 聊天对象Id
     */
    fun getLatestMessageByChatId(chatId: Int): Flow<ChatMessageEntity?>

    /**
     * 分页查询
     */
    fun getPagingByCid(
        chatId: Int,
        uid: Int? = null,
        pageQuery: PageQuery
    ): Flow<List<ChatMessageEntity>>

    suspend fun saveMessage(messageEntity: ChatMessageEntity): Result<Int>
    suspend fun deleteMessage(id: Int): Result<Unit>
    suspend fun updateMessage(id: Int, msg: String? = null, status: MessageStatus? = null): Result<Unit>

    suspend fun sendMessage(modelName: String, message: ChatMessage): Result<Int>

    suspend fun resendMessage(modelName: String, messageId: Int): Result<Int>

    suspend fun unifyMessage(vararg id:Int, selected:Int?=null):Result<Unit>

    fun fetchChatById(id: Int): Flow<ChatEntity?>

    suspend fun saveChat(chatEntity: ChatEntity): Result<Int>

    suspend fun deleteChat(id: Int): Result<Unit>

    suspend fun clearAllMessageByChatId(chatId: Int): Result<Unit>
}
package com.ando.chathouse.data.source

import androidx.paging.PagingSource
import com.ando.chathouse.domain.entity.ChatEntity
import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.PageQuery
import kotlinx.coroutines.flow.Flow

interface ChatDataSource {
    fun getRecentChatPagingSource():PagingSource<Int, ChatMessageEntity>
    fun getMessagePagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity>
    fun getLatestMessageByChatId(chatId: Int): Flow<ChatMessageEntity?>
    fun getMessageById(messageId:Int):Flow<ChatMessageEntity?>
    fun getMessagePagingByCid(chatId: Int, uid: Int?=null, pageQuery: PageQuery):Flow<List<ChatMessageEntity>>
    suspend fun insertMessage(chatMessageEntity: ChatMessageEntity): Int
    suspend fun deleteMessage(id: Int)
    suspend fun updateMessage(id: Int, status: MessageStatus?=null, msg:String?=null, selected: Int?=null)
    suspend fun unifyMessage(vararg id: Int, selected: Int?=null)
    suspend fun shiftStatus(originStatus:MessageStatus, targetStatus:MessageStatus)
    fun getTotalByCid(chatId: Int, uid: Int?=null): Flow<Int>

    fun loadChatById(id: Int): Flow<ChatEntity?>
    suspend fun saveChat(chatEntity: ChatEntity):Int
    suspend fun deleteChat(id: Int)
    suspend fun clearChatMessage(chatId: Int)

}
package com.ando.tastechatgpt.data.source

import androidx.paging.PagingSource
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.ChatStatusPojo
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.PageQuery
import kotlinx.coroutines.flow.Flow

interface ChatDataSource {
    fun getRecentChatPagingSource():PagingSource<Int, ChatMessageEntity>
    fun getPagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity>
    fun getLatestByChatId(chatId: Int): Flow<ChatMessageEntity?>
    fun getPagingByCidAndUid(chatId: Int, uid: Int, pageQuery: PageQuery):Flow<List<ChatMessageEntity>>
    suspend fun insert(chatMessageEntity: ChatMessageEntity): Int
    suspend fun delete(id: Int)
    suspend fun update(id: Int, status: MessageStatus?=null, msg:String?=null)
    suspend fun shiftStatus(originStatus:MessageStatus, targetStatus:MessageStatus)
}
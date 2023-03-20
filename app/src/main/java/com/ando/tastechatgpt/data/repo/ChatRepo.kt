package com.ando.tastechatgpt.data.repo

import androidx.paging.PagingSource
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.ChatStatusPojo
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.ChatMessage
import com.ando.tastechatgpt.domain.pojo.PageQuery
import kotlinx.coroutines.flow.Flow

interface ChatRepo {
    val availableModelList:List<String>

    fun getRecentChatPagingSource():PagingSource<Int, ChatMessageEntity>
    /**
     * 获取聊天对象的聊天分页
     * @param chatId: 聊天对象Id
     */
    fun getPagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity>

    /**
     * 获取聊天对象的最新一条消息
     * @param chatId: 聊天对象Id
     */
    fun getLatestByChatId(chatId: Int): Flow<ChatMessageEntity?>

    /**
     * 分页查询
     */
    fun getPagingByCidAndUid(chatId: Int, uid: Int, pageQuery: PageQuery):Flow<List<ChatMessageEntity>>

    suspend fun save(messageEntity: ChatMessageEntity): Int
    fun deleteMessage(id: Int)
    fun update(id:Int, msg:String?=null, status:MessageStatus?=null)
    fun sendMessage(modelName: String, message: ChatMessage)
}
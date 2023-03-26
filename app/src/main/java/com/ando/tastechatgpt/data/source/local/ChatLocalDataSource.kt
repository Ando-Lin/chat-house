package com.ando.tastechatgpt.data.source.local

import androidx.paging.PagingSource
import com.ando.tastechatgpt.AppDataBase
import com.ando.tastechatgpt.data.source.ChatDataSource
import com.ando.tastechatgpt.data.source.local.dao.ChatDao
import com.ando.tastechatgpt.domain.entity.ChatEntity
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.IntId
import com.ando.tastechatgpt.domain.pojo.PageQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatLocalDataSource @Inject constructor(db: AppDataBase) : ChatDataSource {
    private val chatDao: ChatDao = db.chatDao()

    override fun getRecentChatPagingSource(): PagingSource<Int, ChatMessageEntity> {
        return chatDao.loadRecentChatPagingSource()
    }

    override fun getMessagePagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity> {
        return chatDao.loadMessagePagingSourceByChatId(chatId = chatId)
    }

    override fun getLatestMessageByChatId(chatId: Int): Flow<ChatMessageEntity?> {
        return chatDao.loadLatestMessageByChatId(chatId = chatId)
    }

    override fun getMessageById(messageId: Int): Flow<ChatMessageEntity?> {
        return chatDao.loadMessageById(messageId)
    }


    override fun getMessagePagingByCid(
        chatId: Int,
        uid: Int?,
        pageQuery: PageQuery
    ): Flow<List<ChatMessageEntity>> {
        //pageQuery的order未实现
        val offset =  (pageQuery.page - 1)*pageQuery.pageSize
        return if (uid==null){
            chatDao.loadPagingMessageByCid(chatId = chatId, pageSize = pageQuery.pageSize, pageOffset = offset)
        }else{
            chatDao.loadPagingMessageByCidAndUid(
                chatId = chatId,
                uid = uid,
                pageSize = pageQuery.pageSize,
                pageOffset = (pageQuery.page - 1)*pageQuery.pageSize,
            )
        }
    }


    override suspend fun insertMessage(chatMessageEntity: ChatMessageEntity): Int {
        return chatDao.insertMessage(chatMessageEntity)[0].toInt()
    }

    override suspend fun deleteMessage(id: Int) {
        chatDao.deleteMessageById(IntId(id = id))
    }

    override suspend fun unifyMessage(vararg id: Int, selected: Int?) {
        selected?.let {
            chatDao.updateSelectedState(*id, selected = it)
        }
    }

    override suspend fun updateMessage(id: Int, status: MessageStatus?, msg: String?, selected: Int?) {
        status?.let {
            chatDao.updateMessageStatus(id, it)
        }
        msg?.let {
            chatDao.updateMessage(id, it)
        }
        selected?.let {
            chatDao.updateSelectedState(id, selected = it)
        }
    }

    override suspend fun shiftStatus(originStatus: MessageStatus, targetStatus: MessageStatus) {
        chatDao.shiftMessageStatus(originStatus, targetStatus)
    }

    override fun getTotalByCid(chatId: Int, uid: Int?):Flow<Int> {
        return flow {
            val total = if (uid==null)
                chatDao.getTotalByCid(chatId)
            else
                chatDao.getTotalByCidAndUid(chatId, uid)
            emit(total.toInt())
        }
    }

    override fun loadChatById(id: Int): Flow<ChatEntity?> {
        return chatDao.loadChatById(id)
    }

    override suspend fun saveChat(chatEntity: ChatEntity): Int {
        return chatDao.saveChat(chatEntity).toInt()
    }

    override suspend fun deleteChat(id: Int) {
        chatDao.deleteChat(IntId(id))
    }

    override suspend fun clearChatMessage(chatId: Int) {
        chatDao.clearChatMessage(chatId)
    }
}
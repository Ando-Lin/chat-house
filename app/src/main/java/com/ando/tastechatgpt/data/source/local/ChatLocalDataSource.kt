package com.ando.tastechatgpt.data.source.local

import androidx.paging.PagingSource
import com.ando.tastechatgpt.AppDataBase
import com.ando.tastechatgpt.data.source.ChatDataSource
import com.ando.tastechatgpt.data.source.local.dao.ChatDao
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.ChatStatusPojo
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.IntId
import com.ando.tastechatgpt.domain.pojo.PageQuery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatLocalDataSource @Inject constructor(db: AppDataBase) : ChatDataSource {
    private val chatDao: ChatDao = db.chatDao()

    override fun getRecentChatPagingSource(): PagingSource<Int, ChatMessageEntity> {
        return chatDao.loadRecentChatPagingSource()
    }

    override fun getPagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity> {
        return chatDao.loadPagingSourceByChatId(chatId = chatId)
    }

    override fun getLatestByChatId(chatId: Int): Flow<ChatMessageEntity?> {
        return chatDao.loadLatestByChatId(chatId = chatId)
    }

    override fun getPagingByCidAndUid(
        chatId: Int,
        uid: Int,
        pageQuery: PageQuery
    ): Flow<List<ChatMessageEntity>> {
        //pageQuery的order未实现
        return chatDao.loadPagingByCidAndUid(
            chatId = chatId,
            uid = uid,
            pageSize = pageQuery.pageSize,
            pageOffset = (pageQuery.page - 1)*pageQuery.pageSize,
        )
    }


    override suspend fun insert(chatMessageEntity: ChatMessageEntity): Int {
        return chatDao.insert(chatMessageEntity)[0].toInt()
    }

    override suspend fun delete(id: Int) {
        chatDao.delete(IntId(id = id))
    }

    override suspend fun update(id: Int, status: MessageStatus?, msg: String?) {
        status?.let {
            chatDao.updateStatus(id, it)
        }
        msg?.let {
            chatDao.updateMessage(id, it)
        }
    }

    override suspend fun shiftStatus(originStatus: MessageStatus, targetStatus: MessageStatus) {
        chatDao.shiftStatus(originStatus, targetStatus)
    }
}
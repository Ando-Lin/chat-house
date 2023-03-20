package com.ando.tastechatgpt.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.ChatStatusPojo
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.IntId
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("select * from chat_msg c1 where c1.timestamp = (select max(c2.timestamp) from chat_msg c2 where c1.chat_id=c2.chat_id)")
    fun loadRecentChatPagingSource():PagingSource<Int, ChatMessageEntity>

    /**
     * 时间降序分页获取所有消息
     * @param chatId: 聊天id
     * @return 分页资源
     */
    @Query("SELECT * FROM chat_msg WHERE chat_id = :chatId ORDER BY timestamp DESC")
    fun loadPagingSourceByChatId(chatId: Int): PagingSource<Int, ChatMessageEntity>

    /**
     * 获取最新的一条记录
     * @param chatId: 聊天id
     * @return
     */
    @Query("SELECT * FROM chat_msg WHERE chat_id = :chatId ORDER BY timestamp DESC LIMIT 1")
    fun loadLatestByChatId(chatId: Int): Flow<ChatMessageEntity?>

    /**
     * 分页查询
     */
    @Query("SELECT * FROM chat_msg WHERE chat_id = :chatId AND uid = :uid ORDER BY timestamp DESC "
            +" LIMIT :pageSize OFFSET :pageOffset")
    fun loadPagingByCidAndUid(
        chatId: Int,
        uid: Int,
        pageSize: Int,
        pageOffset: Int,
    ):Flow<List<ChatMessageEntity>>

    @Query("update chat_msg set status = :status where id = :id")
    suspend fun updateStatus(id:Int, status: MessageStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ChatMessageEntity::class)
    suspend fun insert(vararg chatMessageEntities: ChatMessageEntity): List<Long>

    @Delete(entity = ChatMessageEntity::class)
    suspend fun delete(id: IntId)

    @Query("update chat_msg set status = :targetStatus where status = :originStatus")
    suspend fun shiftStatus(originStatus:MessageStatus, targetStatus:MessageStatus)

    @Query("update chat_msg set text = :message where id = :id")
    suspend fun updateMessage(id:Int, message:String)
}


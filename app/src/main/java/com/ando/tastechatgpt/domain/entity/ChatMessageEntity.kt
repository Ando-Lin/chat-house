package com.ando.tastechatgpt.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * @param id: 主键
 * @param chatId: 对话关联的uid，与谁对话就关联谁
 * @param uid: 发出消息的用户
 * @param text: 消息文本
 * @param secondDiff: 与上一条消息的秒差
 * @param status: 消息发送状态
 */
@Entity(tableName = "chat_msg")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "chat_id", index = true)
    val chatId: Int,
    @ColumnInfo(index = true)
    val uid: Int,
    val text: String,
    val timestamp: LocalDateTime,
    @ColumnInfo(name = "second_diff")
    val secondDiff: Long,
    val status: MessageStatus = MessageStatus.Success,
    @ColumnInfo(defaultValue = "false")
    val selected: Int = 0,
)

data class ChatStatusPojo(
    val id: Int,
    val status: MessageStatus
)

enum class MessageStatus {
    Success,
    Failed,
    Sending,
    Reading,
    Interrupt
}
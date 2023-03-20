package com.ando.tastechatgpt.domain.entity

import androidx.compose.runtime.Composable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ando.tastechatgpt.R
import java.time.LocalDateTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "chat_id") val chatId: Int,
    val uid: Int,
    val text: String,
    val timestamp: LocalDateTime,
    @ColumnInfo(name = "second_diff") val secondDiff: Long,
    val status: MessageStatus = MessageStatus.Success
)

data class ChatStatusPojo(
    val id: Int,
    val status: MessageStatus
)

enum class MessageStatus(val value: Int, val ui: @Composable () -> Unit) {
    Success(0, { }),
    Failed(
        1,
        {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_failed),
                tint = MaterialTheme.colorScheme.error,
                contentDescription = null
            )
        }),
    Sending(2, {
        com.ando.tastechatgpt.ui.component.CycleAnimation(
            iconRes = R.drawable.ic_retry, isClockwise = false
        )
    });
}
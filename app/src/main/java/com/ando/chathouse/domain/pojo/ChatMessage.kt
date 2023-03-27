package com.ando.chathouse.domain.pojo

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import java.time.LocalDateTime

data class ChatMessage(
    val chatId: Int,
    val uid: Int,
    val text: String,
    val timestamp: LocalDateTime,
    val secondDiff: Long,
    val status: MessageStatus = MessageStatus.Success
)

fun ChatMessage.toEntity():ChatMessageEntity=
    ChatMessageEntity(
        chatId = this.chatId,
        uid = this.uid,
        text = this.text,
        timestamp = this.timestamp,
        secondDiff = this.secondDiff,
        status = this.status
    )

package com.ando.chathouse.domain.pojo

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



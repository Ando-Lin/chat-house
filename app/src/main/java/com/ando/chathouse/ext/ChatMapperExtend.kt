package com.ando.chathouse.ext

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.pojo.ChatMessage

fun ChatMessage.toEntity(): ChatMessageEntity =
    ChatMessageEntity(
        chatId = this.chatId,
        uid = this.uid,
        text = this.text,
        timestamp = this.timestamp,
        secondDiff = this.secondDiff,
        status = this.status
    )
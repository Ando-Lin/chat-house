package com.ando.chathouse.strategy

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.pojo.ChatContext

interface CarryMessageStrategy {
    fun filter(message: ChatMessageEntity, chatContext: ChatContext):Boolean
}
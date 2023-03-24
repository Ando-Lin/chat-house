package com.ando.tastechatgpt.strategy

import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.pojo.ChatContext

interface CarryMessageStrategy {
    fun filter(message: ChatMessageEntity, chatContext: ChatContext):Boolean
}
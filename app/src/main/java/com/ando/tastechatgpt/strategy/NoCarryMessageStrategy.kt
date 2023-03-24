package com.ando.tastechatgpt.strategy

import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.pojo.ChatContext

class NoCarryMessageStrategy:CarryMessageStrategy {
    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        return false
    }

    companion object{
        const val NAME = "no carry"
    }
}
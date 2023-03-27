package com.ando.chathouse.strategy

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.pojo.ChatContext

class NoCarryMessageStrategy:CarryMessageStrategy {
    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        return false
    }

    companion object{
        const val NAME = "no carry"
    }
}
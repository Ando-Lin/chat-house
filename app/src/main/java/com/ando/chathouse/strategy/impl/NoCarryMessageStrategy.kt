package com.ando.chathouse.strategy.impl

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.pojo.ChatContext
import com.ando.chathouse.strategy.CarryMessageStrategy

class NoCarryMessageStrategy: CarryMessageStrategy {
    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        return false
    }

    companion object{
        const val NAME = "不携带"
    }
}
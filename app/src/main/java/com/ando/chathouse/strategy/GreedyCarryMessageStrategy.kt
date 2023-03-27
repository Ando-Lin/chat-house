package com.ando.chathouse.strategy

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.ChatContext

class GreedyCarryMessageStrategy:CarryMessageStrategy {
    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        return message.status == MessageStatus.Success
    }

    companion object{
        const val NAME = "greedy"
    }
}
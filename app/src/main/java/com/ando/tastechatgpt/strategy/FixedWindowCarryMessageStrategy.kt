package com.ando.tastechatgpt.strategy

import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.ChatContext

class FixedWindowCarryMessageStrategy:StatefulCarryMessageStrategy {
    //TODO: 窗口大小读取
    private val windowSize:Int = 10
    private var counter: Int = 0

    override fun clearState() {
        counter = 0
    }

    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        return message.status == MessageStatus.Success && counter++ < windowSize
    }

    companion object{
        const val NAME = "fixed window"
        private const val TAG = "FixedWindowCarryMessage"
    }
}
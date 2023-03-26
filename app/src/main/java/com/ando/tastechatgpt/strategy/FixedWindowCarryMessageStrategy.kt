package com.ando.tastechatgpt.strategy

import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.ChatContext
import java.util.concurrent.atomic.AtomicInteger

class FixedWindowCarryMessageStrategy() :MutableStatefulCarryMessageStrategy<Int> {
    override val defaultState: Int = 10
    private var windowSize:Int = defaultState
    private var counter: AtomicInteger = AtomicInteger(0)

    override fun setState(value: Int) {
        windowSize = value
    }

    override fun clearState() {
        counter.set(0)
    }

    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        return message.status == MessageStatus.Success && counter.getAndIncrement() < windowSize
    }

    companion object{
        const val NAME = "fixed window"
        private const val TAG = "FixedWindowCarryMessage"
    }
}
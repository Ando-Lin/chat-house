package com.ando.chathouse.strategy.impl

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.ChatContext
import com.ando.chathouse.strategy.MutableStatefulCarryMessageStrategy
import java.util.concurrent.atomic.AtomicInteger

class FixedWindowCarryMessageStrategy() : MutableStatefulCarryMessageStrategy<Int> {
    override val defaultState: Int = 12
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
        private const val TAG = "FixedWindowCarryMessage"
        val NAME = FixedWindowCarryMessageStrategy::class.simpleName!!
    }
}
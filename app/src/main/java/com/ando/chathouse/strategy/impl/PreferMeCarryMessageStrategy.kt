package com.ando.chathouse.strategy.impl

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.ChatContext
import com.ando.chathouse.strategy.MutableStatefulCarryMessageStrategy
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 偏好我的策略。只收集ai固定条目消息和我的所有消息
 */
class PreferMeCarryMessageStrategy: MutableStatefulCarryMessageStrategy<Int> {
    override val defaultState: Int = 3
    private var hasCollectAiLastMessage: AtomicBoolean = AtomicBoolean(false)
    private var remainAIMessage:Int = defaultState
    private var counter: AtomicInteger = AtomicInteger(0)

    override fun setState(value: Int) {
        remainAIMessage = value
    }

    override fun clearState() {
        hasCollectAiLastMessage.set(false)
    }

    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        return when{
            message.status != MessageStatus.Success && message.status != MessageStatus.Interrupt -> false
            message.uid == chatContext.myUid -> true
            else -> counter.getAndIncrement() < remainAIMessage
        }
    }

    companion object {
        val NAME:String = PreferMeCarryMessageStrategy::class.simpleName!!
        private const val TAG = "PreferMeCarryMessageStr"
    }
}
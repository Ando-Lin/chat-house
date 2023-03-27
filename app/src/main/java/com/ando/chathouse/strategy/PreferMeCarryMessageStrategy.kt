package com.ando.chathouse.strategy

import android.util.Log
import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.MessageStatus
import com.ando.chathouse.domain.pojo.ChatContext
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
        if (message.status != MessageStatus.Success)
            return false
        if (message.uid == chatContext.myUid)
            return true
        Log.i(TAG, "filter: counter = ${counter.get()}")
        return counter.getAndIncrement() < remainAIMessage
    }

    companion object {
        private const val TAG = "PreferMeCarryMessageStr"
        const val NAME = "prefer me"
    }
}
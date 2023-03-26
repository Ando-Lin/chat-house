package com.ando.tastechatgpt.strategy

import android.util.Log
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.ChatContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 偏好我的策略。只收集ai固定条目消息和我的所有消息
 */
class PreferMeCarryMessageStrategy(val remainAIMessage:Int = 3) : StatefulCarryMessageStrategy {
    private var hasCollectAiLastMessage: AtomicBoolean = AtomicBoolean(false)
    private val counter: AtomicInteger = AtomicInteger(remainAIMessage)

    override fun clearState() {
        hasCollectAiLastMessage.set(false)
    }

    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        if (message.status != MessageStatus.Success)
            return false
        if (message.uid == chatContext.myUid)
            return true
        Log.i(TAG, "filter: counter = ${counter.get()}")
        return counter.getAndDecrement() > 0
    }

    companion object {
        private const val TAG = "PreferMeCarryMessageStr"
        const val NAME = "prefer me"
    }
}
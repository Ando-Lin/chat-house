package com.ando.tastechatgpt.strategy

import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.MessageStatus
import com.ando.tastechatgpt.domain.pojo.ChatContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 偏好我的策略。只收集ai最新一条消息和我的所有消息
 */
class PreferMeCarryMessageStrategy:StatefulCarryMessageStrategy {
    private var hasCollectAiLastMessage: AtomicBoolean = AtomicBoolean(false)

    override fun clearState() {
        hasCollectAiLastMessage.set(false)
    }

    override fun filter(message: ChatMessageEntity, chatContext: ChatContext): Boolean {
        if (message.status != MessageStatus.Success)
            return false
        if (message.uid == chatContext.myUid)
            return true
        return if (!hasCollectAiLastMessage.get()){
            hasCollectAiLastMessage.set(true)
            true
        }else{
            false
        }
    }

    companion object{
        const val NAME = "prefer me"
    }
}
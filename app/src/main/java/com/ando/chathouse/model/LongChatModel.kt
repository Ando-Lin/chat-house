package com.ando.chathouse.model

import com.ando.chathouse.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow

interface LongChatModel : ChatModel {
    /**
     * 发送消息到ai模型
     * @param messages: 发送到模型的消息
     * @param para: 模型参数
     * @return ai模型响应的消息
     */
    fun sendMessages(messages: List<RoleMessage>, para: ChatModel.Para?=null): Flow<String?>

    override fun sendMessage(message: String, para: ChatModel.Para?): Flow<String?> {
        return sendMessages(
            messages = listOf(
                RoleMessage(
                    role = RoleMessage.USER_ROLE,
                    content = message
                )
            ), para = para
        )
    }
}
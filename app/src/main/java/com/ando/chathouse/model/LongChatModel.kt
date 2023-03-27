package com.ando.chathouse.model

import com.ando.chathouse.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow

interface LongChatModel : ChatModel {
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
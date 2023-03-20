package com.ando.tastechatgpt.model

import android.graphics.ColorSpace.Model
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow

interface ChatModelManger {
    val models:List<ChatModel>
    fun addModel(chatModel: ChatModel):ChatModelManger
    fun sendMessage(modelName: String, para: ChatModel.Para, message: String): Flow<String?>
    fun sendMessages(
        modelName: String,
        para: ChatModel.Para,
        messages: List<RoleMessage>
    ): Flow<String?>
}
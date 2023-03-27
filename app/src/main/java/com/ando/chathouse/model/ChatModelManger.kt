package com.ando.chathouse.model

import com.ando.chathouse.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow

interface ChatModelManger {
    val models:Map<String, Lazy<ChatModel>>
    fun addModel(name:String, model:Lazy<ChatModel>):ChatModelManger
    fun sendMessage(modelName: String, para: ChatModel.Para, message: String): Flow<String?>
    fun sendMessages(
        modelName: String,
        para: ChatModel.Para,
        messages: List<RoleMessage>
    ): Flow<String?>
}
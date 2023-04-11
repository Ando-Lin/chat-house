package com.ando.chathouse.model

import kotlinx.coroutines.flow.Flow

interface ChatModel {
    val label:String

    /**
     * 发送消息到ai模型
     * @param message: 发送到模型的消息
     * @param para: 模型参数
     * @return ai模型响应的消息
     */
    fun sendMessage(message:String,para: Para?):Flow<String?>
    data class Para(val apiKey: String?)
}
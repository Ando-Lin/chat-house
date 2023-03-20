package com.ando.tastechatgpt.model

import kotlinx.coroutines.flow.Flow

interface ChatModel {
    val name:String
    fun sendMessage(message:String,para: Para?):Flow<String?>
    data class Para(val apiKey: String?)
}
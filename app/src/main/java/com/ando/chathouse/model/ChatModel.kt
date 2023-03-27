package com.ando.chathouse.model

import kotlinx.coroutines.flow.Flow

interface ChatModel {
    val label:String
    fun sendMessage(message:String,para: Para?):Flow<String?>
    data class Para(val apiKey: String?)
}
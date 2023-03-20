package com.ando.tastechatgpt.model

import com.ando.tastechatgpt.data.api.ForchangeApi
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient

class ForchangeModel(override val httpClient: OkHttpClient):AbstractLongChatModel() {
    override val baseUrl: String
        get() = "https://api.forchange.cn/"
    override val name: String
        get() = modelName
    private val api = retrofit.create(ForchangeApi::class.java)

    override fun sendMessages(messages: List<RoleMessage>, para: ChatModel.Para?): Flow<String?> {
        return flow {
            val response = api.query(Data(messages))
            val choice = response.body()?.choices?.get(0)
            val message = choice?.message
            val text = choice?.text
            emit(message?.content?:text)
        }
    }

    companion object{
        const val modelName = "forchange"
    }

    data class Data(
        val messages: List<RoleMessage>,
        val model:String = "gpt-3.5-turbo"
    )
}
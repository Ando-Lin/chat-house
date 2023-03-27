package com.ando.chathouse.model

import com.ando.chathouse.data.api.Authorization
import com.ando.chathouse.data.api.ChatGPTCompletionPara
import com.ando.chathouse.data.api.OpenAIApi
import com.ando.chathouse.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient

class OpenAIGPT3d5Model internal constructor(
    override val baseUrl: String,
    override val httpClient: OkHttpClient?,
    private val needAPIKey: Boolean = true,
) : AbstractLongChatModel(), RequireOpenAIAPIKey {
    override val label: String = LABEL
    private val api = retrofit.create(OpenAIApi::class.java)
    override fun sendMessages(messages: List<RoleMessage>, para: ChatModel.Para?): Flow<String?> {
        val apiKey = para?.apiKey
        if (needAPIKey && apiKey.isNullOrBlank()) {
            throw IllegalArgumentException("缺少APIKey")
        }

        return flow {
            val response = api.queryChatGPT(
                Authorization(apiKey!!),
                ChatGPTCompletionPara(messages = messages)
            )
            val body = response.body()
            emit(body?.choices?.get(0)?.message?.content)
        }
    }

    companion object {
        private const val TAG = "OpenAIGPT3d5Model"
        const val  LABEL= "OpenAI"
        private val cache = mutableMapOf<String, OpenAIGPT3d5Model>()
        fun create(
            baseUrl: String,
            needAPIKey: Boolean = true,
            httpClient: OkHttpClient? = null
        ): OpenAIGPT3d5Model {
            return cache.getOrPut(baseUrl) {
                OpenAIGPT3d5Model(baseUrl, needAPIKey = needAPIKey, httpClient = httpClient)
            }
        }
    }

}
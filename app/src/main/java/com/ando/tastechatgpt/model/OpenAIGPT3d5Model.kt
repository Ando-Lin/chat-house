package com.ando.tastechatgpt.model

import com.ando.tastechatgpt.constant.OPENAI_URL
import com.ando.tastechatgpt.data.api.Authorization
import com.ando.tastechatgpt.data.api.ChatGPTCompletionPara
import com.ando.tastechatgpt.data.api.OpenAIApi
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient

class OpenAIGPT3d5Model(
    override val httpClient: OkHttpClient,
) :AbstractLongChatModel() {
    override val name: String = modelName
    override val baseUrl: String = OPENAI_URL
    private val api = retrofit.create(OpenAIApi::class.java)

    override fun sendMessages(messages: List<RoleMessage>, para: ChatModel.Para?): Flow<String?> {
        return flow {
            val apiKey = para?.apiKey?:throw IllegalArgumentException("$name 模型必须包含apikey")
            val response = api.queryChatGPT(Authorization(apiKey), ChatGPTCompletionPara(message=messages))
            emit(response.choices[0].message.content)
        }
    }

    companion object{
        const val modelName = "openai-gpt3.5"
    }
}
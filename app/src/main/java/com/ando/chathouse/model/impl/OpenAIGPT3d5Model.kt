package com.ando.chathouse.model.impl

import android.util.Log
import com.ando.chathouse.data.api.OpenAIApi
import com.ando.chathouse.domain.pojo.Authorization
import com.ando.chathouse.domain.pojo.ChatGPTCompletionPara
import com.ando.chathouse.domain.pojo.RoleMessage
import com.ando.chathouse.model.AbstractLongChatModel
import com.ando.chathouse.model.ChatModel
import com.ando.chathouse.model.RequireOpenAIAPIKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.HttpException

class OpenAIGPT3d5Model internal constructor(
    override val baseUrl: String,
    override val httpClient: OkHttpClient?,
    private val isFullUrl: Boolean = false,
    private val needAPIKey: Boolean = true,
) : AbstractLongChatModel(), RequireOpenAIAPIKey {
    override val label: String = LABEL
    private val api = retrofit.create(OpenAIApi::class.java)
    override fun sendMessages(messages: List<RoleMessage>, para: ChatModel.Para?): Flow<String?> {
        val apiKey = when (needAPIKey) {
             true -> {
                 para?.apiKey.also {
                     if (it.isNullOrBlank())
                         throw IllegalArgumentException("缺少APIKey")
                 }
             }
            else -> null
        }


        return flow {
            val response = when(isFullUrl){
                true -> api.queryChatGPTNotStandard(
                    baseUrl,
                    apiKey?.let { Authorization(it) },
                    ChatGPTCompletionPara(messages = messages)
                )
                else -> api.queryChatGPT(
                    apiKey?.let { Authorization(it) },
                    ChatGPTCompletionPara(messages = messages)
                )
            }
            val body = response.body()
            Log.i(TAG, "sendMessages: response body = $body")
            if (body!=null){
                emit(body.choices[0].message.content)
            }else{
                throw HttpException(response)
            }
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
                OpenAIGPT3d5Model(baseUrl, needAPIKey = needAPIKey, httpClient = httpClient, isFullUrl = false)
            }
        }

        fun createNotStandard(
            fullUrl: String, needAPIKey: Boolean = true, httpClient: OkHttpClient? = null
        ): OpenAIGPT3d5Model {
            return cache.getOrPut(fullUrl) {
                OpenAIGPT3d5Model(fullUrl, needAPIKey = needAPIKey, httpClient = httpClient, isFullUrl = true)
            }
        }
    }

}
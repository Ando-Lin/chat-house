package com.ando.chathouse.model.impl

import android.util.Log
import com.ando.chathouse.data.api.OpenAIApi
import com.ando.chathouse.domain.pojo.Authorization
import com.ando.chathouse.domain.pojo.ChatGPTCompletionPara
import com.ando.chathouse.domain.pojo.RoleMessage
import com.ando.chathouse.exception.MessageStreamInterruptException
import com.ando.chathouse.model.AbstractLongChatModel
import com.ando.chathouse.model.ChatModel
import com.ando.chathouse.model.RequireOpenAIAPIKey
import kotlinx.coroutines.flow.*
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class StreamOpenAIGPT3d5Model internal constructor(
    override val baseUrl: String,
    private val isFullUrl: Boolean = false,
    private val needAPIKey: Boolean = true,
    httpClient: OkHttpClient? = null
) : AbstractLongChatModel(), RequireOpenAIAPIKey {
    override val label: String = LABEL
    override val httpClient: OkHttpClient =
        httpClient ?: OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .cacheControl(CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
                .build()
            chain.proceed(request)
        }.build()

    private val api = retrofit.create(OpenAIApi::class.java)
    private val pattern = Pattern.compile("\"content\":\"([^\"]+)\"")

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

        return flow<String?> {
            val response = when(isFullUrl){
                true -> api.streamChatGPTNotStandard(
                    baseUrl, apiKey?.let { Authorization(it) }, ChatGPTCompletionPara(messages = messages, stream = true)
                )
                else -> api.streamChatGPT(
                    apiKey?.let { Authorization(it) }, ChatGPTCompletionPara(messages = messages, stream = true)
                )
            }
            val body = response.body() ?: throw HttpException(response)
            body.use {
                val source = it.source()
                val buffer = Buffer()
                var string = ""
                while (!source.exhausted()) {
                    val readBytes = source.read(buffer, 8196)
                    string = buffer.readString(Charsets.UTF_8)
                    Log.i(TAG, "sendMessages: readStream = --start $string --end")
                    emit(string)
                }
                if (string.lastIndexOf("data: [DONE]") == -1) {
                    throw MessageStreamInterruptException()
                }
            }

        }.transform {
            val flow = it?.split("\n")?.asFlow()
            emitAll(flow ?: flowOf(null))
        }.transform {
            if (!it.isNullOrBlank()) emit(it)
        }.map {
            val matcher = pattern.matcher(it)
            return@map when (matcher.find()) {
                true -> matcher.group(1)
                else -> null
            }
        }.buffer(capacity = 200)
    }

    companion object {
        private const val TAG = "StreamOpenAIGPT3d5Model"
        const val LABEL = "OpenAI"
        private val cache = mutableMapOf<String, StreamOpenAIGPT3d5Model>()
        fun create(
            baseUrl: String, needAPIKey: Boolean = true, httpClient: OkHttpClient? = null
        ): StreamOpenAIGPT3d5Model {
            return cache.getOrPut(baseUrl) {
                StreamOpenAIGPT3d5Model(baseUrl, needAPIKey = needAPIKey, httpClient = httpClient, isFullUrl = false)
            }
        }

        fun createNotStandard(
            fullUrl: String, needAPIKey: Boolean = true, httpClient: OkHttpClient? = null
        ): StreamOpenAIGPT3d5Model {
            return cache.getOrPut(fullUrl) {
                StreamOpenAIGPT3d5Model(fullUrl, needAPIKey = needAPIKey, httpClient = httpClient, isFullUrl = true)
            }
        }

    }

}
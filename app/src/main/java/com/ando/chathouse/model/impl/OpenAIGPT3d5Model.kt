package com.ando.chathouse.model.impl

import android.util.Log
import com.ando.chathouse.constant.CHAT_COMPLETIONS_URL_PATH
import com.ando.chathouse.data.api.OpenAIApi
import com.ando.chathouse.domain.pojo.Authorization
import com.ando.chathouse.domain.pojo.ChatGPTCompletionPara
import com.ando.chathouse.domain.pojo.RoleMessage
import com.ando.chathouse.exception.MessageStreamInterruptException
import com.ando.chathouse.model.AbstractLongChatModel
import com.ando.chathouse.model.ChatModel
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.HttpException
import java.util.regex.Pattern

class OpenAIGPT3d5Model constructor(
    override val baseUrl: String,
    private val urlPath: String = CHAT_COMPLETIONS_URL_PATH,
    override val httpClient: OkHttpClient?,
    private val needAPIKey: Boolean = true,
    private val stream: Boolean = false,
) : AbstractLongChatModel(){
    override val label: String = LABEL
    private val api = retrofit.create(OpenAIApi::class.java)
    private val pattern = Pattern.compile("\"content\":\"([^\"]+)\"")

    override fun sendMessages(messages: List<RoleMessage>, para: ChatModel.Para?): Flow<String?> {
        //如果不需要key则不填充，避免泄漏
        val apiKey = when (needAPIKey) {
            true -> {
                para?.apiKey.also {
                    if (it.isNullOrBlank())
                        throw IllegalArgumentException("缺少APIKey")
                }
            }
            else -> null
        }

        return when (stream) {
            false -> flow {
                val response = api.queryChatGPT(
                    urlPath,
                    apiKey?.let { Authorization(it) },
                    ChatGPTCompletionPara(messages = messages)
                )
                val body = response.body() ?: throw HttpException(response)
                emit(body.choices[0].message.content)
            }
            else -> flow<String?> {
                val response = api.streamChatGPT(
                    urlPath,
                    apiKey?.let { Authorization(it) },
                    ChatGPTCompletionPara(messages = messages, stream = true)
                )
                val body = response.body() ?: throw HttpException(response)
                body.use {
                    val source = it.source()
                    val buffer = Buffer()
                    var string = ""
                    while (!source.exhausted()) {
                        source.read(buffer, 8196)
                        string = buffer.readString(Charsets.UTF_8)
                        Log.i(TAG, "sendMessages: readStream = --start $string --end")
                        emit(string)
                    }
                    if (string.lastIndexOf("data: [DONE]") == -1) {
                        throw MessageStreamInterruptException()
                    }
                }
            }
                .buffer(capacity = 200)
                .transform {
                    val flow = it?.split("\n")?.asFlow()
                    if (flow!=null) emitAll(flow)
                }
                .map {
                    val matcher = pattern.matcher(it)
                    when (matcher.find()) {
                        true -> matcher.group(1)
                        else -> null
                    }
                }
        }
    }

    companion object {
        private const val TAG = "OpenAIGPT3d5Model"
        const val LABEL = "OpenAI"
    }

}
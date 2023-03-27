package com.ando.chathouse.domain.pojo

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatGPTCompletionResponse(
    val id: String,
    val created: Long,
    val model: String?,
    val choices: List<Choice>,
    val usage: Usage?,
    @Json(name = "object")
    val obj: String,
) {
    /**
     * 回复格式
     *
    {
    'id': 'chatcmpl-6p9XYPYSTTRi0xEviKjjilqrWU2Ve',
    'object': 'chat.completion',
    'created': 1677649420,
    'model': 'gpt-3.5-turbo',
    'usage': {'prompt_tokens': 56, 'completion_tokens': 31, 'total_tokens': 87},
    'choices': [
    {
    'message': {
    'role': 'assistant',
    'content': 'The 2020 World Series was played in Arlington, Texas at the Globe Life Field, which was the new home stadium for the Texas Rangers.'},
    'finish_reason': 'stop',
    'index': 0
    }
    ]
    }
     */
    @JsonClass(generateAdapter = true)
    data class Choice(
        val index: Int,
        val message: RoleMessage,
        @Json(name = "finish_reason") val finishReason: String,
    )

    data class Usage(
        @Json(name = "prompt_tokens")
        val promptTokens: Int,
        @Json(name = "completion_tokens")
        val completionTokens: Int,
        @Json(name = "total_tokens")
        val totalTokens: Int
    )

}



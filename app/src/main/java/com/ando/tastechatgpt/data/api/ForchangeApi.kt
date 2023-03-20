package com.ando.tastechatgpt.data.api

import com.ando.tastechatgpt.domain.pojo.ChatGPTCompletionResponse
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import com.ando.tastechatgpt.model.ForchangeModel
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ForchangeApi {
    @POST("/")
    suspend fun query(@Body data: ForchangeModel.Data): Response<FResponse>

    /**
     * {
     * "created":0,
     * "object":"",
     * "id":"",
     * "choices":[{"text":"你在我的面前开什么玩笑？你应该知道你在哪里，你需要尊重我作为女王的身
    份（用严肃的语气）。如果你不知道如何尊重我，请离开我的视线！",
    "index":0,
    "finish_reason":"stop",
    "message":{"role":"assistant",
    "content":"你在我的面前开什么玩笑？你应该知道你在哪里，你需要尊重我作为女王的身份（用严肃的语气）。如果你不知道如何尊重我，请离开我的视线！"},
    "block":false}
    ],
    "error":null}
     */
    data class FResponse(
        val id: String?,
        val created: Int?,
        @Json(name = "object") val obj: String?,
        val choices: List<Choice>,
        val error: String?
    )

    data class Choice(
        val text: String,
        val index: Int?,
        @Json(name = "finish_reason") val finishReason: String,
        val message: RoleMessage?,
        val block: Boolean
    )
}
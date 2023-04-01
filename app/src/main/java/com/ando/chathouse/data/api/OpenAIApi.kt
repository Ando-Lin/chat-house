package com.ando.chathouse.data.api

import com.ando.chathouse.domain.pojo.Authorization
import com.ando.chathouse.domain.pojo.ChatGPTCompletionPara
import com.ando.chathouse.domain.pojo.ChatGPTCompletionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OpenAIApi {


    /**
     * @param authorization: 认证
     * @param para: 参数
     */
    @POST("/v1/chat/completions")
    suspend fun queryChatGPT(
        @Header("Authorization") authorization: Authorization,
        @Body para: ChatGPTCompletionPara
    ): Response<ChatGPTCompletionResponse>

    /**
     * @param authorization: 认证
     * @param para: 参数
     */
    @Streaming
    @POST("/v1/chat/completions")
    suspend fun streamChatGPT(
        @Header("Authorization") authorization: Authorization,
        @Body para: ChatGPTCompletionPara
    ): ResponseBody

}




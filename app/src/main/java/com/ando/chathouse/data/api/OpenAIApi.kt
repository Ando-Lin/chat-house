package com.ando.chathouse.data.api

import com.ando.chathouse.domain.pojo.Authorization
import com.ando.chathouse.domain.pojo.ChatGPTCompletionPara
import com.ando.chathouse.domain.pojo.ChatGPTCompletionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OpenAIApi {


    /**
     * @param authorization: 认证
     * @param para: 参数
     */
    @POST("/v1/chat/completions")
    suspend fun queryChatGPT(
        @Header("Authorization") authorization: Authorization?=null,
        @Body para: ChatGPTCompletionPara
    ): Response<ChatGPTCompletionResponse>

    /**
     * @param authorization: 认证
     * @param para: 参数
     */
    @Streaming
    @POST("/v1/chat/completions")
    suspend fun streamChatGPT(
        @Header("Authorization") authorization: Authorization?=null,
        @Body para: ChatGPTCompletionPara
    ): Response<ResponseBody>


    /**
     * @param url: 完整url
     * @param authorization: 认证
     * @param para: 参数
     */
    @Streaming
    @POST
    suspend fun streamChatGPTNotStandard(
        @Url url:String,
        @Header("Authorization") authorization: Authorization?=null,
        @Body para: ChatGPTCompletionPara
    ): Response<ResponseBody>

    /**
     * @param url: 完整url
     * @param authorization: 认证
     * @param para: 参数
     */
    @POST
    @Headers(value = ["Accept: application/json", "Content-Type: application/json"])
    suspend fun queryChatGPTNotStandard(
        @Url url:String,
        @Header("Authorization") authorization: Authorization?=null,
        @Body para: ChatGPTCompletionPara
    ): Response<ChatGPTCompletionResponse>

}




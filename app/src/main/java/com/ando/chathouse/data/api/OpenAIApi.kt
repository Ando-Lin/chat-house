package com.ando.chathouse.data.api

import com.ando.chathouse.domain.pojo.Authorization
import com.ando.chathouse.domain.pojo.ChatGPTCompletionPara
import com.ando.chathouse.domain.pojo.ChatGPTCompletionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OpenAIApi {



    /**
     * @param url: url
     * @param authorization: 认证
     * @param para: 参数
     */
    @Streaming
    @POST
    suspend fun streamChatGPT(
        @Url url:String,
        @Header("Authorization") authorization: Authorization?=null,
        @Body para: ChatGPTCompletionPara
    ): Response<ResponseBody>

    /**
     * @param url: url
     * @param authorization: 认证
     * @param para: 参数
     */
    @POST
    @Headers(value = ["Accept: application/json", "Content-Type: application/json"])
    suspend fun queryChatGPT(
        @Url url:String,
        @Header("Authorization") authorization: Authorization?=null,
        @Body para: ChatGPTCompletionPara
    ): Response<ChatGPTCompletionResponse>

}




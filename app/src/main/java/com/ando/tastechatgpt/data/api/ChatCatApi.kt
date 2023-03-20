package com.ando.tastechatgpt.data.api

import com.ando.tastechatgpt.model.ChatCatModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatCatApi {
    @POST("/api/generate")
    suspend fun query(
        @Body data: ChatCatModel.Data
    ):Response<String>
}
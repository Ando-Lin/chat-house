package com.ando.tastechatgpt.domain.pojo

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoleMessage(
    val role: String,
    val content: String
){
    companion object{
        const val USER_ROLE = "user"
        const val ASSISTANT_ROLE = "assistant"
        const val SYSTEM_ROLE = "system"
    }
}
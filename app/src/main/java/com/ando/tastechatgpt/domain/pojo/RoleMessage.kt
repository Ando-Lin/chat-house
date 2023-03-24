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

        fun userMessage(content: String) = RoleMessage(USER_ROLE, content)
        fun assistantMessage(content: String) = RoleMessage(ASSISTANT_ROLE, content)
        fun systemMessage(content: String) = RoleMessage(SYSTEM_ROLE, content)
    }

}
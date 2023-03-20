package com.ando.tastechatgpt.domain.pojo

import android.net.Uri
import com.ando.tastechatgpt.domain.entity.UserEntity
import java.time.LocalDateTime

data class User(
    val id: Int,
    val name: String,
    val avatar: Uri?,
    val description:String
)

fun User.toUserEntity(createTime: LocalDateTime) =
    UserEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        createTime = createTime,
        avatar = this.avatar
    )

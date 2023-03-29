package com.ando.chathouse.ext

import com.ando.chathouse.domain.entity.UserEntity
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.domain.pojo.UserDetail
import com.ando.chathouse.domain.pojo.UserExtraInfo
import java.time.LocalDateTime

fun UserDetail.toEntity() =
    UserEntity(
        id = id,
        name = name,
        avatar = avatar,
        description = description,
        enableGuide = enableGuide,
        enableReminder = enableReminder,
        reminder = reminder,
        createTime = createTime
    )

fun User.toUserEntity(createTime: LocalDateTime) =
    UserEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        createTime = createTime,
        avatar = this.avatar,
    )


fun UserEntity.toUser() =
    User(
        id = this.id,
        name = this.name,
        description = this.description,
        avatar = this.avatar
    )

fun UserEntity.toUserDetail() =
    UserDetail(
        id = id,
        name = name,
        avatar = avatar,
        description = description,
        enableGuide = enableGuide,
        enableReminder = enableReminder,
        reminder = reminder,
        createTime = createTime
    )

fun UserEntity.toUserExtrasInfo() =
    UserExtraInfo(
        id = id,
        enableReminder = enableReminder,
        enableGuide = enableGuide,
        reminder = reminder
    )

fun UserEntity.toUserAndExtras():Pair<User, UserExtraInfo> {
    val user = this.toUser()
    val extraInfo = this.toUserExtrasInfo()
    return user to extraInfo
}
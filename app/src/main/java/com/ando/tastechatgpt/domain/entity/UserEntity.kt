package com.ando.tastechatgpt.domain.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ando.tastechatgpt.domain.pojo.User
import com.ando.tastechatgpt.domain.pojo.UserDetail
import java.time.LocalDateTime

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val avatar: Uri?,
    val description: String,
    @ColumnInfo(name = "enable_guide", defaultValue = "true")
    val enableGuide: Boolean = true,
    @ColumnInfo(name = "enable_reminder", defaultValue = "false")
    val enableReminder: Boolean = false,
    @ColumnInfo(defaultValue = "")
    val reminder: String = "",
    @ColumnInfo(name="create_time", index = true)
    val createTime: LocalDateTime
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

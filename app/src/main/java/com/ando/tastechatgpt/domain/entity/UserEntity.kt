package com.ando.tastechatgpt.domain.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ando.tastechatgpt.domain.pojo.User
import java.time.LocalDateTime

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val avatar: Uri?,
    val description: String,
    @ColumnInfo(name="create_time") val createTime: LocalDateTime
)

fun UserEntity.toUser() =
    User(
        id = this.id,
        name = this.name,
        description = this.description,
        avatar = this.avatar
    )

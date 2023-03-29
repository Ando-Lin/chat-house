package com.ando.chathouse.domain.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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



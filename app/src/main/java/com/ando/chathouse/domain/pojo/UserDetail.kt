package com.ando.chathouse.domain.pojo

import android.net.Uri
import androidx.room.ColumnInfo
import java.time.LocalDateTime

data class UserDetail(
    val id: Int = 0,
    val name: String = "",
    val avatar: Uri? = null,
    val description: String = "",
    @ColumnInfo(name = "enable_guide")
    val enableGuide: Boolean = true,
    @ColumnInfo(name = "enable_reminder")
    val enableReminder: Boolean = false,
    @ColumnInfo()
    val reminder: String = "",
    @ColumnInfo(name="create_time", index = true)
    val createTime: LocalDateTime = LocalDateTime.now()
)




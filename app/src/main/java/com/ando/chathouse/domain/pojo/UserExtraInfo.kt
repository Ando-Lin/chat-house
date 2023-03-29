package com.ando.chathouse.domain.pojo

import androidx.room.ColumnInfo

data class UserExtraInfo(
    val id:Int,
    @ColumnInfo(name = "enable_guide")
    val enableGuide: Boolean = true,
    @ColumnInfo(name = "enable_reminder")
    val enableReminder: Boolean = false,
    @ColumnInfo
    val reminder: String = "",
)

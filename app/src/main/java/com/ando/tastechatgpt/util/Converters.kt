package com.ando.tastechatgpt.util

import android.net.Uri
import androidx.room.TypeConverter
import com.ando.tastechatgpt.domain.entity.MessageStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value:Long?):LocalDateTime?{
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.of("+8")) }
    }

    @TypeConverter
    fun localDateTimeToTimestamp(value: LocalDateTime?):Long?{
        return value?.let { value.toInstant(ZoneOffset.of("+8")).toEpochMilli() }
    }

    @TypeConverter
    fun fromStringStatus(value:String): MessageStatus {
        return MessageStatus.valueOf(value)
    }

    @TypeConverter
    fun enumStatusToStringStatus(messageStatus: MessageStatus): String {
        return messageStatus.name
    }

    @TypeConverter
    fun fromUriString(value:String?):Uri?{
        return value?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun uriToString(value: Uri?):String?{
        return value?.toString()
    }
}
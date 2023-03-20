package com.ando.tastechatgpt

import androidx.room.Database
import androidx.room.Entity
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ando.tastechatgpt.data.source.local.dao.ChatDao
import com.ando.tastechatgpt.data.source.local.dao.UserDao
import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.entity.UserEntity
import com.ando.tastechatgpt.util.Converters

@Database(
    entities = [ChatMessageEntity::class, UserEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDataBase:RoomDatabase() {
    abstract fun chatDao():ChatDao
    abstract fun userDao():UserDao
}
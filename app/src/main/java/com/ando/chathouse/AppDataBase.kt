package com.ando.chathouse

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.ando.chathouse.data.source.local.dao.ChatDao
import com.ando.chathouse.data.source.local.dao.UserDao
import com.ando.chathouse.domain.entity.ChatEntity
import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.entity.UserEntity
import com.ando.chathouse.strategy.impl.PreferMeCarryMessageStrategy
import com.ando.chathouse.util.Converters

@Database(
    entities = [ChatMessageEntity::class, UserEntity::class, ChatEntity::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = AppDataBase.AutoMigration2_3::class),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDataBase:RoomDatabase() {
    abstract fun chatDao():ChatDao
    abstract fun userDao():UserDao

    class AutoMigration2_3: AutoMigrationSpec{
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            val sqLiteQuery = SupportSQLiteQueryBuilder
                .builder("user")
                .columns(arrayOf("id"))
                .create()
            val cursor = db.query(sqLiteQuery)
            while (cursor.moveToNext()){
                val uid = cursor.getInt(0)
                val chatEntity = ContentValues()
                chatEntity.put("id", uid)
                chatEntity.put("uid", uid)
                chatEntity.put("msg_strategy", PreferMeCarryMessageStrategy.NAME)
                db.insert("chat", SQLiteDatabase.CONFLICT_IGNORE, chatEntity)
            }
        }
    }

}
package com.ando.chathouse.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ando.chathouse.strategy.PreferMeCarryMessageStrategy

@Entity(
    tableName = "chat",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["uid"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION,
        deferred = false
    )]
)
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "msg_strategy")
    val messageStrategy: String,
    @ColumnInfo(index = true)
    val uid: Int,
//    val title:String
) {
    companion object {
        //uid和chatId一一对应
        fun individual(uid: Int): ChatEntity =
            ChatEntity(id = uid, messageStrategy = PreferMeCarryMessageStrategy.NAME, uid = uid)
    }
}
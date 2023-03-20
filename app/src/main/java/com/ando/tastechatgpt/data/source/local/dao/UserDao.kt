package com.ando.tastechatgpt.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ando.tastechatgpt.domain.entity.UserEntity
import com.ando.tastechatgpt.domain.pojo.IntId
import com.ando.tastechatgpt.domain.pojo.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user ORDER BY create_time ASC")
    fun getPagingSource():PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user WHERE id = :id")
    fun loadById(id:Int):Flow<UserEntity?>

    @Insert(entity = UserEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userEntity: UserEntity):Long

    @Update(entity = UserEntity::class)
    suspend fun update(user: User)

    @Delete(entity = UserEntity::class)
    suspend fun deleteById(id:IntId)
}
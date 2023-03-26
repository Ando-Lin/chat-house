package com.ando.tastechatgpt.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.ando.tastechatgpt.domain.entity.UserEntity
import com.ando.tastechatgpt.domain.pojo.IntId
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user ORDER BY create_time ASC")
    fun getPagingSource():PagingSource<Int, UserEntity>

    @Query("SELECT * FROM user WHERE id = :id")
    fun loadById(id:Int):Flow<UserEntity?>

    @Insert(entity = UserEntity::class)
    suspend fun insert(userEntity: UserEntity):Long

    @Update(entity = UserEntity::class)
    suspend fun update(userEntity: UserEntity)

    @Delete(entity = UserEntity::class)
    suspend fun deleteById(id:IntId)
}
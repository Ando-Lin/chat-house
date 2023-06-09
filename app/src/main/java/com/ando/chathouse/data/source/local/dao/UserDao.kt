package com.ando.chathouse.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.ando.chathouse.domain.entity.UserEntity
import com.ando.chathouse.domain.pojo.IntId
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.domain.pojo.UserExtraInfo
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

    @Update(entity = UserEntity::class)
    suspend fun updateUser(user: User)

    @Update(entity = UserEntity::class)
    suspend fun updateUserExtras(userExtraInfo: UserExtraInfo)

    @Delete(entity = UserEntity::class)
    suspend fun deleteById(id:IntId)
}
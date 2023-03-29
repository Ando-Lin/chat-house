package com.ando.chathouse.data.repo

import androidx.paging.PagingSource
import com.ando.chathouse.domain.entity.UserEntity
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.domain.pojo.UserExtraInfo
import kotlinx.coroutines.flow.Flow

interface UserRepo {
    fun getPagingSource():PagingSource<Int, UserEntity>
    fun fetchById(id: Int):Flow<UserEntity?>
    suspend fun deleteById(id: Int): Result<Unit>
    suspend fun save(user: UserEntity): Result<Int>
    suspend fun update(entity: UserEntity?=null, user: User?=null, extrasInfo: UserExtraInfo?=null): Result<Unit>
}
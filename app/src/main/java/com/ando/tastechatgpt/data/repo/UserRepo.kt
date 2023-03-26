package com.ando.tastechatgpt.data.repo

import androidx.paging.PagingSource
import com.ando.tastechatgpt.domain.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepo {
    fun getPagingSource():PagingSource<Int, UserEntity>
    fun fetchById(id: Int):Flow<UserEntity?>
    suspend fun deleteById(id: Int): Result<Unit>
    suspend fun save(user: UserEntity): Result<Int>
    suspend fun update(user: UserEntity): Result<Unit>
}
package com.ando.tastechatgpt.data.source

import androidx.paging.PagingSource
import com.ando.tastechatgpt.domain.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    fun getPagingSource():PagingSource<Int, UserEntity>
    fun getById(id:Int):Flow<UserEntity?>
    suspend fun save(user:UserEntity):Int
    suspend fun update(user: UserEntity)
    suspend fun deleteById(id:Int)
}
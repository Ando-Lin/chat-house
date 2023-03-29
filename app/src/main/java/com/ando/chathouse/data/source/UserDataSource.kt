package com.ando.chathouse.data.source

import androidx.paging.PagingSource
import com.ando.chathouse.domain.entity.UserEntity
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.domain.pojo.UserExtraInfo
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    fun getPagingSource():PagingSource<Int, UserEntity>
    fun getById(id:Int):Flow<UserEntity?>
    suspend fun save(user:UserEntity):Int
    suspend fun update(entity: UserEntity?=null,user:User?=null,extrasInfo:UserExtraInfo?=null)
    suspend fun deleteById(id:Int)
}
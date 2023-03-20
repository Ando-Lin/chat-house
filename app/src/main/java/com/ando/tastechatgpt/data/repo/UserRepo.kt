package com.ando.tastechatgpt.data.repo

import androidx.paging.PagingSource
import com.ando.tastechatgpt.domain.entity.UserEntity
import com.ando.tastechatgpt.domain.pojo.User
import kotlinx.coroutines.flow.Flow

interface UserRepo {
    fun getPagingSource():PagingSource<Int, UserEntity>
    fun fetchById(id: Int):Flow<UserEntity?>
    fun deleteById(id: Int)
    fun save(user: UserEntity)
    fun update(user: User)
}
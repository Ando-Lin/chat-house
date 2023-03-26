package com.ando.tastechatgpt.data.source.local

import androidx.paging.PagingSource
import com.ando.tastechatgpt.AppDataBase
import com.ando.tastechatgpt.data.source.UserDataSource
import com.ando.tastechatgpt.data.source.local.dao.UserDao
import com.ando.tastechatgpt.domain.entity.UserEntity
import com.ando.tastechatgpt.domain.pojo.IntId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    private val dataBase: AppDataBase,
) : UserDataSource {
    private val userDao: UserDao = dataBase.userDao()

    override fun getPagingSource(): PagingSource<Int, UserEntity> {
        return userDao.getPagingSource()
    }

    override fun getById(id: Int): Flow<UserEntity?> {
        return userDao.loadById(id)
    }

    override suspend fun save(user: UserEntity): Int {
        return userDao.insert(user).toInt()
    }

    override suspend fun update(user: UserEntity){
        userDao.update(user)
    }

    override suspend fun deleteById(id: Int){
        userDao.deleteById(IntId(id))
    }
}
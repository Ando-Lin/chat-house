package com.ando.chathouse.data.source.local

import androidx.paging.PagingSource
import com.ando.chathouse.AppDataBase
import com.ando.chathouse.data.source.UserDataSource
import com.ando.chathouse.data.source.local.dao.UserDao
import com.ando.chathouse.domain.entity.UserEntity
import com.ando.chathouse.domain.pojo.IntId
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.domain.pojo.UserExtraInfo
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

    override suspend fun update(entity: UserEntity?, user: User?, extrasInfo: UserExtraInfo?) {
        entity?.let {
            userDao.update(entity)
        }
        user?.let {
            userDao.updateUser(user)
        }
        extrasInfo?.let {
            userDao.updateUserExtras(extrasInfo)
        }
    }

    override suspend fun deleteById(id: Int){
        userDao.deleteById(IntId(id))
    }
}
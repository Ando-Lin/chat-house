package com.ando.chathouse.data.repo

import androidx.paging.PagingSource
import com.ando.chathouse.constant.MY_UID
import com.ando.chathouse.data.source.local.UserLocalDataSource
import com.ando.chathouse.di.IoDispatcher
import com.ando.chathouse.domain.entity.UserEntity
import com.ando.chathouse.domain.pojo.User
import com.ando.chathouse.domain.pojo.UserExtraInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

class UserRepoImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val external: CoroutineScope
) : UserRepo {

    init {
        external.launch {
            //检查是否存在“本人”的记录.防止误删导致错误
            fetchById(MY_UID).collect {
                if (it != null) {
                    return@collect
                }
                //不存在则添加
                save(
                    UserEntity(
                        id = MY_UID,
                        name = "",
                        avatar = null,
                        description = "",
                        createTime = LocalDateTime.now()
                    )
                )
            }
        }
    }

    override fun getPagingSource(): PagingSource<Int, UserEntity> {
        return localDataSource.getPagingSource()
    }

    override fun fetchById(id: Int): Flow<UserEntity?> {
        return localDataSource.getById(id = id).flowOn(ioDispatcher)
    }

    override suspend fun deleteById(id: Int): Result<Unit> {
        return withContext(external.coroutineContext + ioDispatcher) {
            kotlin.runCatching {
                localDataSource.deleteById(id)
            }
        }
    }

    override suspend fun save(user: UserEntity): Result<Int> {
        return withContext(external.coroutineContext + ioDispatcher) {
            kotlin.runCatching {
                localDataSource.save(user)
            }
        }
    }

    override suspend fun update(
        entity: UserEntity?,
        user: User?,
        extrasInfo: UserExtraInfo?
    ): Result<Unit> {
        return withContext(external.coroutineContext + ioDispatcher) {
            kotlin.runCatching {
                localDataSource.update(entity,user,extrasInfo)
            }
        }
    }

}
package com.ando.tastechatgpt.data.repo

import androidx.paging.PagingSource
import com.ando.tastechatgpt.constant.HUMAN_UID
import com.ando.tastechatgpt.data.source.local.UserLocalDataSource
import com.ando.tastechatgpt.di.IoDispatcher
import com.ando.tastechatgpt.domain.entity.UserEntity
import com.ando.tastechatgpt.domain.pojo.User
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
            //检查是否存在一号用户，即用户本人
            fetchById(HUMAN_UID).collect {
                if (it != null) {
                    return@collect
                }
                //不存在则添加一号用户
                save(
                    UserEntity(
                        id = HUMAN_UID,
                        name = "用户",
                        avatar = null,
                        description = "",
                        createTime = LocalDateTime.now()
                    )
                )
            }
        }
        external.launch {
            //添加角色设计师
            val designerId = HUMAN_UID+1
            fetchById(designerId).collect{
                if (it != null) {
                    return@collect
                }
                //不存在则添加用户
                save(
                    UserEntity(
                        id = designerId,
                        name = "角色设计师",
                        avatar = null,
                        description = "你是角色设计师，你擅长通过模糊的描述给出极细细致的人物描述，包括性格、外貌、口吻、小动作等",
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

    override suspend fun update(user: User): Result<Unit> {
        return withContext(external.coroutineContext + ioDispatcher) {
            kotlin.runCatching {
                localDataSource.update(user)
            }
        }
    }

}
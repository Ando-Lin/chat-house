package com.ando.chathouse.di

import com.ando.chathouse.data.repo.ChatRepo
import com.ando.chathouse.data.repo.ChatRepoImpl
import com.ando.chathouse.data.repo.UserRepo
import com.ando.chathouse.data.repo.UserRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindChatRepo(impl: ChatRepoImpl):ChatRepo
    @Binds
    abstract fun bindUserRepo(impl: UserRepoImpl):UserRepo
}
package com.ando.tastechatgpt.di

import com.ando.tastechatgpt.data.repo.ChatRepo
import com.ando.tastechatgpt.data.repo.ChatRepoImpl
import com.ando.tastechatgpt.data.repo.UserRepo
import com.ando.tastechatgpt.data.repo.UserRepoImpl
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
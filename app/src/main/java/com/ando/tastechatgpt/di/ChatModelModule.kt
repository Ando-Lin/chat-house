package com.ando.tastechatgpt.di

import com.ando.tastechatgpt.data.api.OpenAIApi
import com.ando.tastechatgpt.model.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModelModule {
    @Provides
    fun provideChatMangerImpl(okHttpClient: OkHttpClient, openAIApi: OpenAIApi):ChatModelMangerImpl{
        return ChatModelMangerImpl().apply {
            addModel(ChatCatModel(okHttpClient))
            addModel(OpenAIGPT3d5Model(okHttpClient))
            addModel(ForchangeModel(okHttpClient))
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatMangerModule{
    @Binds
    abstract fun bindChatMangerModule(chatModelMangerImpl: ChatModelMangerImpl):ChatModelManger
}
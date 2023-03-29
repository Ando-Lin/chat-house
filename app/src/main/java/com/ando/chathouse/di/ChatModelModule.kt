package com.ando.chathouse.di

import com.ando.chathouse.constant.OPENAI_MIRROR_URL
import com.ando.chathouse.constant.OPENAI_URL
import com.ando.chathouse.model.*
import com.ando.chathouse.model.impl.ChatModelMangerImpl
import com.ando.chathouse.model.impl.StreamOpenAIGPT3d5Model
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ChatModelModule {
    @Provides
    fun provideChatMangerImpl(okHttpClient: OkHttpClient): ChatModelMangerImpl {
        return ChatModelMangerImpl().apply {
//            addModel("openAI GPT3.5", lazy { OpenAIGPT3d5Model.create(OPENAI_URL, httpClient = okHttpClient) })
//            addModel("openAI GPT3.5 mirror", lazy { OpenAIGPT3d5Model.create(OPENAI_MIRROR_URL, httpClient = okHttpClient) })
            addModel("openAI GPT3.5", lazy { StreamOpenAIGPT3d5Model.create(OPENAI_URL, httpClient = okHttpClient) })
            addModel("openAI GPT3.5镜像", lazy { StreamOpenAIGPT3d5Model.create(OPENAI_MIRROR_URL, httpClient = okHttpClient) })
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatMangerModule{
    @Binds
    abstract fun bindChatMangerModule(chatModelMangerImpl: ChatModelMangerImpl):ChatModelManger
}
package com.ando.tastechatgpt.di

import com.ando.tastechatgpt.constant.OPENAI_URL
import com.ando.tastechatgpt.data.api.OpenAIApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(timeout = 60, unit = TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
//            .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress()))
            .build()

    @Provides
    fun provideOpenAIApi(httpClient: OkHttpClient): OpenAIApi =
        Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl(OPENAI_URL)
            .build()
            .create(OpenAIApi::class.java)

}
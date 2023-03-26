package com.ando.tastechatgpt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(timeout = 30, unit = TimeUnit.SECONDS)
            .callTimeout(timeout = 30, unit = TimeUnit.SECONDS)
            .readTimeout(timeout = 30, unit = TimeUnit.SECONDS)
            .addInterceptor {
                val newReq = it.request().newBuilder()
                    .header("User-Agent", System.getProperty("http.agent")?:"Android")
//                    .header("Accept-Encoding", "deflate")
                    .header("Accept", "application/json")
                    .header("Connection", "keep-alive")
                    .build()
                it.proceed(newReq)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

}
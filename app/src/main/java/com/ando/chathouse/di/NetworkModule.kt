package com.ando.chathouse.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(timeout = 20, unit = TimeUnit.SECONDS)
            .readTimeout(timeout = 40, unit = TimeUnit.SECONDS)
            .writeTimeout(timeout = 40, unit = TimeUnit.SECONDS)
            .addInterceptor {
                val newReq = it.request().newBuilder()
                    .header("User-Agent", System.getProperty("http.agent") ?: "Android")
                    .header("Accept", "application/json")
                    .header("Connection", "keep-alive")
                    .build()
                it.proceed(newReq)
            }
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .cacheControl(CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .build()


}
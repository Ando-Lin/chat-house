package com.ando.tastechatgpt.di

import android.content.Context
import androidx.room.Room
import com.ando.tastechatgpt.AppDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {
    @Provides
    fun provideDataBase(@ApplicationContext context: Context):AppDataBase =
        Room.databaseBuilder(context, AppDataBase::class.java, "local_db").build()
}
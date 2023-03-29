package com.ando.chathouse.di

import android.content.Context
import androidx.room.Room
import com.ando.chathouse.AppDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {
    @Provides
    fun provideDataBase(@ApplicationContext context: Context):AppDataBase =
        Room.databaseBuilder(context, AppDataBase::class.java, "local_db")
            .createFromAsset("database/default.db")
            .build()
}
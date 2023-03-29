package com.ando.chathouse.di

import android.util.Log
import com.tencent.bugly.crashreport.CrashReport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher():CoroutineDispatcher = Dispatchers.IO

    @Provides
    fun providesCoroutineScope(exceptionHandler: CoroutineExceptionHandler): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)

    @Provides
    fun providesCoroutineExceptionHandler(): CoroutineExceptionHandler =
        CoroutineExceptionHandler { context, throwable ->
            Log.e(TAG, "providesCoroutineExceptionHandler: context=$context", throwable)
            CrashReport.postCatchedException(throwable)
        }

    private const val TAG = "CoroutinesModule"
}
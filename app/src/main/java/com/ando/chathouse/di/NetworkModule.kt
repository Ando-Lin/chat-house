package com.ando.chathouse.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.BufferedSource
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


    class ChunksInterceptor(private val callback: (BufferedSource) -> Unit) : Interceptor {
        //okhttp可能并发，给多个订阅者可能会造成混乱
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalResponse = chain.proceed(chain.request())
            val contentType = originalResponse.header("content-type") ?: ""
            if (!contentType.contentEquals("text/event-stream")) {
                return originalResponse
            }
            val responseBody = originalResponse.body
            val source = responseBody!!.source()

            val buffer = Buffer() // We create our own Buffer

            // Returns true if there are no more bytes in this source
//            while (!source.exhausted()) {
//                val readBytes = source.read(buffer, Long.MAX_VALUE) // We read the whole buffer
//                val data = buffer.readString(Charsets.UTF_8)
//
//                println("Read: $readBytes bytes")
//                println("Content: \n--$data--\n")
//            }

//            originalResponse
//                .newBuilder()
//                .body(
//                    ResponseBody.create(responseBody.contentType(), responseBody.contentLength())
//                {sink->
//
//                })

            return originalResponse
        }
    }
}
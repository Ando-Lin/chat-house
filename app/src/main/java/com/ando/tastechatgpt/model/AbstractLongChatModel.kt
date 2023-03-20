package com.ando.tastechatgpt.model

import com.ando.tastechatgpt.data.api.ChatCatApi
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

abstract class AbstractLongChatModel : LongChatModel {
    abstract val baseUrl: String
    open val httpClient: OkHttpClient? = null
    open var moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    open val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
//            .addConverterFactory(ScalarsConverterFactory.create())
            .apply { httpClient?.let { this.client(it) } }
            .build()
}
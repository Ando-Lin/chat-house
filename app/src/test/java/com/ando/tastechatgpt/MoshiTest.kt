package com.ando.tastechatgpt

import com.ando.tastechatgpt.model.ChatCatModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert
import org.junit.Test
import retrofit2.converter.moshi.MoshiConverterFactory

class MoshiTest {
    @Test
    fun test1(){
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(ChatCatModel.Data::class.java)
        val data = ChatCatModel.Data(messages = listOf(), setting = ChatCatModel.Setting(""))
        val json = jsonAdapter.toJson(data)
        print(json)
        Assert.assertTrue(json.isNotBlank())
    }

    @Test
    fun test2(){
        val factory = MoshiConverterFactory.create()
    }
}
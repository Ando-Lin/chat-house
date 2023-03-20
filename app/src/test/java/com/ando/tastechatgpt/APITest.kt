package com.ando.tastechatgpt

import com.ando.tastechatgpt.constant.DEFAULT_API_KEY
import com.ando.tastechatgpt.constant.OPENAI_URL
import com.ando.tastechatgpt.data.api.Authorization
import com.ando.tastechatgpt.data.api.OpenAIApi
import com.ando.tastechatgpt.data.api.TextCompletionPara
import com.ando.tastechatgpt.di.NetworkModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.Retrofit

class APITest {
//    private val retrofit: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(OPENAI_URL)
//            .addConverterFactory(GsonConverterFactory.create(NetworkModule.getGson()))
//            .client(NetworkModule.provideHttpClient())
//            .build()
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun testQuery() = runTest{
//        val api = retrofit.create(OpenAIApi::class.java)
//        val response = api.query(
//            authorization = Authorization(DEFAULT_API_KEY),
//            para = TextCompletionPara(prompt = "hello")
//        )
//        assert(response.choices[0].text.isNotBlank())
//    }
}
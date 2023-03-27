package com.ando.chathouse

import com.ando.chathouse.constant.OPENAI_URL
import com.ando.chathouse.data.api.Authorization
import com.ando.chathouse.data.api.ChatGPTCompletionPara
import com.ando.chathouse.domain.pojo.RoleMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test1() {
        val now = LocalDateTime.now()
        val zonedDateTime = ZonedDateTime.of(now, ZoneId.systemDefault())
        println(zonedDateTime.toEpochSecond()*1000)
        println(System.currentTimeMillis())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test2()= runTest{
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(timeout = 60, unit = TimeUnit.SECONDS)
            .callTimeout(timeout = 60, unit = TimeUnit.SECONDS)
            .readTimeout(timeout = 60, unit = TimeUnit.SECONDS)
            .writeTimeout(timeout = 60, unit = TimeUnit.SECONDS)
//            .addInterceptor {
//                val newReq = it.request().newBuilder()
//                    .header("User-Agent", System.getProperty("http.agent") ?: "Android")
//                    .header("Accept-Encoding", "deflate")
////                    .header("Connection", "keep-alive")
//                    .build()
//                it.proceed(newReq)
//            }
//            .addInterceptor(HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            })
            .build()
        val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(ChatGPTCompletionPara::class.java)
        val authorization =
            Authorization(apiKey = "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")
        val para =
            ChatGPTCompletionPara(messages = listOf(RoleMessage.userMessage("你好")), stream = true)

        val request = Request.Builder()
            .header("Authorization", "Bearer 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")
            .header("Content-Type", "application/json")
            .url("$OPENAI_URL/v1/chat/completions")
            .post(adapter.toJson(para).toRequestBody())
            .build()

        val newCall = okHttpClient.newCall(request = request)

        val execute = newCall.execute()

        foo(execute.body!!)


//        newCall.enqueue(object : Callback{
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//
//            }
//
//        })
    }

    fun foo(body: ResponseBody){
        println(body)
//        val body = openAIApi.streamChatGPT(authorization, para)
        val bufferedSource = body!!.source()
        val buffer = Buffer()
        val stringBuilder = StringBuilder()
        while (!bufferedSource.exhausted()){
            val read = bufferedSource.read(buffer, 8192)
            val s = buffer.readUtf8()
            println("buffer = $s")
            stringBuilder.append(s)
            println("strbuilder = $stringBuilder")
            buffer.clear()
        }
    }

    @Test
    fun test3() {
        val toByte = " data: ".toByteArray()
        println("byte.size = ${toByte.size}")
        Assert.assertTrue(" data: ".contentEquals(" data: "))
    }

    @Test
    fun test4() {
        val json = """
            {"id":"chatcmpl-6yMK81IPpg6IL7v5Slr9NijYioXjZ","object":"chat.completion.chunk","created":1679843512,"model":"gpt-3.5-turbo-0301","choices":[{"delta":{"content":"（"},"index":0,"finish_reason":null}]}
        """.trimIndent()
        val pattern = Pattern.compile("\"content\":\"([^\"]+)\"")
        val matcher = pattern.matcher(json)

        if (matcher.find()) {
            val deltaString = matcher.group(1)
            println(deltaString)
        }
    }
}
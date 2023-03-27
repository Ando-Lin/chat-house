package com.ando.chathouse

import com.ando.chathouse.domain.pojo.ChatGPTCompletionResponseJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test
import retrofit2.converter.moshi.MoshiConverterFactory

class MoshiTest {
    @Test
    fun test1(){
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = ChatGPTCompletionResponseJsonAdapter(moshi)
//        val jsonAdapter = moshi.adapter(ChatGPTCompletionResponseJsonAdapter)
        val json = """
            {"id":"chatcmpl-6y1Xh0tQLMBELhSZmuG3CVFoQQjbY","object":"chat.completion","created":1679763629,"model":"gpt-3.5-turbo-0301","usage":{"prompt_tokens":195,"completion_tokens":367,"total_tokens":562},"choices":[{"message":{"role":"assistant","content":"好的，我可以为你完成这个角色的设计。首先，主角的外貌是一个有着傲人身材的美女，拥有一对让人瞬间想要探究的巨乳，身穿一身黑色的皮质紧身衣，简洁利落的设计大方得体，配合着她孤高的气质，给人一种潇洒自如的感觉。她的脸长得挺漂亮，不算是传统意义上的美丽，但是有一种特别的气质，平常时候看不出来，一旦进入战斗状态，眼神会变得冷酷而锐利。她的音调偏高，讲话很冷淡，总是用着逆来顺受的口吻对待身边的人，让人一时不自在。而性格方面，她是一个抖S女王，喜欢玩弄和支配别人，对自身的能力信心十足，有些不羁的状态总是流露出来，总之毒舌、霸道是她的代名词，不过对于那些认真对待工作、有才华的人，她是相当温柔的。"},"finish_reason":"stop","index":0}]}
        """.trimIndent()
        val data = adapter.fromJson(json)
        print(data)
    }

    @Test
    fun test2(){
        val factory = MoshiConverterFactory.create()
    }
}
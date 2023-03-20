package com.ando.tastechatgpt.model

import com.ando.tastechatgpt.data.api.ChatCatApi
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.Instant
import kotlin.random.Random
import kotlin.random.nextULong

class ChatCatModel constructor(
    override val httpClient: OkHttpClient
) : AbstractLongChatModel() {
    override val name: String = modelName
    override val baseUrl = "https://chatcat.pages.dev/"
    private val api = super.retrofit
        .create(ChatCatApi::class.java)

    override fun sendMessages(messages: List<RoleMessage>, para: ChatModel.Para?): Flow<String?> {
        //获取apikey
        val apiKey = para?.apiKey ?: throw IllegalArgumentException("$name 模型必须包含apikey")
        //组装数据并将数据转为json
        val data = Data(
            setting = Setting(openaiAPIKey = apiKey),
            messages = messages
        )
        return flow{
            val response = api.query(data)
            print(response)
            emit(response.body())
        }
    }

    companion object{
        const val modelName = "chatcat"
    }

    /**
     * {
     * "setting":{
     *      "openaiAPIKey":"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",
     *      "role":"女王大人",
     *      "customRule":"",
     *      "openaiAPITemperature":60,
     *      "roleAvatar":""
     *      },
     * "messages":[
     *      {"role":"user","content":"你好"},
     *      {"role":"assistant","content":"低贱的奴才，你终于来了。
     *      女王大人已经等得不耐烦了。快告诉我，你准备为女王大人做些什么？(女王大人盯着你的双眼)"},
     *      {"role":"user","content":"再见"}
     *      ],
     *  "time":1678753986327,
     *  "sign":"d63e1b4b64f0e10582428a544a61ce0ba5450cc294b297a599054795b273077f"}
     * 9f0d2e27fa264f24ea574c22636c46424185cd5014a193b31c82a0c30bed83e4
     */

    @JsonClass(generateAdapter = true)
    data class Setting(
        val openaiAPIKey: String,
        val role: String = Role.Queen.value,
        val customRole: String = "",
        val openaiAPITemperature: Int = 60,
        val roleAvatar: String = ""
    ) {
        enum class Role(val value: String) {
            Default(""),
            Queen("女王大人");

            override fun toString(): String {
                return this.value
            }
        }
    }

    @JsonClass(generateAdapter = true)
    data class Data(
        val messages: List<RoleMessage>,
        val setting: Setting,
    ) {
        @Json(name = "time") var time: Long = 1678846126877
        @Json(name = "sign")
        var sign: String = "ca57206b93fe5ce2e27990a0a1cdfbc46e9dde150eba106a80356178690841b8"
    }


}
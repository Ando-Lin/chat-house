package com.ando.chathouse.data.api

import com.ando.chathouse.domain.pojo.ChatGPTCompletionResponse
import com.ando.chathouse.domain.pojo.RoleMessage
import com.squareup.moshi.Json
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OpenAIApi {


    /**
     * @param authorization: 认证
     * @param para: 参数
     */
    @POST("/v1/chat/completions")
    suspend fun queryChatGPT(
        @Header("Authorization") authorization:Authorization,
        @Body para: ChatGPTCompletionPara
    ):Response<ChatGPTCompletionResponse>

    /**
     * @param authorization: 认证
     * @param para: 参数
     */
    @Streaming
    @POST("/v1/chat/completions")
    suspend fun streamChatGPT(
        @Header("Authorization") authorization:Authorization,
        @Body para: ChatGPTCompletionPara
    ): ResponseBody

}

data class Authorization(
    val apiKey: String
) {
    override fun toString(): String {
        var value = apiKey
        val prefix = "Bearer "
        if (!apiKey.startsWith(prefix = prefix)){
            value = prefix + value
        }
        return value
    }
}


/**
 * chatgpt api所需参数
 * @param model: 模型类型
 * @param messages: 包含上下文的信息
 * @param temperature: 0到2之间。较高的值(如0.8)将使输出更加随机，而较低的值(如0.2)将使输出更加集中和确定。
 * @param topP: 温度采样的另一种替代方法称为核采样，其中模型考虑具有最高p概率质量的标记的结果。所以0.1意味着只考虑包含前10%概率质量的令牌。
 * @param n: 为每个输入消息生成多少个聊天完成选项。
 * @param stop: 最多4个序列，API将停止生成进一步的令牌。
 * @param presencePenalty: 介于-2.0和2.0之间的数字。正值会根据新标记到目前为止是否出现在文本中来惩罚它们，从而增加模型谈论新主题的可能性。
 * @param frequencyPenalty: 介于-2.0和2.0之间的数字。正值会根据新符号在文本中的现有频率来惩罚它们，从而降低模型逐字重复同一行的可能性。
 * @param logitBias: 修改指定令牌在补全中出现的可能性。接受一个json对象，该对象将标记(由标记器中的标记ID指定)映射到从-100到100的关联偏差值。
 * 在数学上，偏差被添加到抽样前由模型生成的对数中。每个模型的确切效果会有所不同，但介于-1和1之间的值应该会减少或增加选择的可能性;像-100或100这样的值应该导致相关令牌的禁止或排他选择。
 */
data class ChatGPTCompletionPara(
    val model: String = GPT_3d5_TURBO,
    val messages: List<RoleMessage>,
    val temperature: Double = 1.0,
    @Json(name = "top_p")
    val topP: Double = 1.0,
    val n:Int = 1,
    val stop:Int? = null,
    val stream: Boolean = false,
    @Json(name = "presence_penalty")
    val presencePenalty:Double = 0.0,
    @Json(name = "frequency_penalty")
    val frequencyPenalty:Double = 0.0,
    @Json(name = "logit_bias")
    val logitBias:Map<String,String>?=null,
){

    companion object{
        const val GPT_3d5_TURBO = "gpt-3.5-turbo"
        const val GPT_3d5_TURBO_0301 = "gpt-3.5-turbo-0301"
    }
}
package com.ando.tastechatgpt.data.api

import android.graphics.ColorSpace
import com.ando.tastechatgpt.domain.pojo.ChatGPTCompletionResponse
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import com.ando.tastechatgpt.domain.pojo.TextCompletionResponse
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {

    /**
     * @param authorization: 认证
     * @param para: 参数
     */
    @POST("/v1/completions")
    suspend fun query(
        @Header("Authorization") authorization:Authorization,
        @Body para: TextCompletionPara
    ):TextCompletionResponse


    /**
     * @param authorization: 认证
     * @param para: 参数
     */
    @POST("/v1/chat/completions")
    suspend fun queryChatGPT(
        @Header("Authorization") authorization:Authorization,
        @Body para: ChatGPTCompletionPara
    ):ChatGPTCompletionResponse
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
 * 参数
 * @param prompt: 提示或者上下文
 * @param maxTokens: 最大生成令牌数。即生成文本长度上限
 * @param n: 生成的候选数
 * @param temperature: 控制生成结果的随机性。值越高，生成结果越随机。
 * @param topP: 控制 API 生成的概率分布。值越低，生成的结果将更加确定
 * @param stream: 控制 API 生成文本的模式。如果设置为 true，则 API 将生成一个生成流，可以逐步生成文本。
 * @param model: 使用的文本模型
 */
data class TextCompletionPara(
    val prompt: String,
    val maxTokens:Int = 2048,
    val temperature: Double = 0.5,
    val model:String = "text-davinci-003",
//    val n:Int = 1,
//    val topP: Double = 1.0,
//    val stream: Boolean = false,
)

/**
 * chatgpt api所需参数
 * @param model: 模型类型
 * @param message: 包含上下文的信息
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
    val model: ModelType = ModelType.GPT_3_5_TURBO,
    val message: List<RoleMessage>,
    val temperature: Double = 1.0,
    val topP: Double = 1.0,
    val n:Int = 1,
    val stop:Int? = null,
    val stream: Boolean = false,
    val presencePenalty:Double = 0.0,
    val frequencyPenalty:Double = 0.0,
    val logitBias:Map<String,String>?=null,

    ){
    enum class ModelType(private val value:String){
        GPT_3_5_TURBO("gpt-3.5-turbo"),
        GPT_3_5_TURBO_0301("gpt-3.5-turbo-0301");

        override fun toString(): String {
            return value
        }
    }
}
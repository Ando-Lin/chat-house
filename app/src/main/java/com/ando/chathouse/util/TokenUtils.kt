package com.ando.chathouse.util

import com.ando.chathouse.domain.pojo.RoleMessage
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.EncodingType

object TokenUtils {
    private val registry = Encodings.newDefaultEncodingRegistry()
    private val enc = registry.getEncoding(EncodingType.CL100K_BASE) //适用于gpt3.5、gpt4
    //每条消息的额外消费
    private const val PER_MESSAGE_CONSUME = 3

    fun computeToken(vararg roleMessage: RoleMessage?):Int{
        //计数器
        var counter = 0
        roleMessage.forEach {
            it?.let {
                counter += enc.countTokens(it.content)
                counter += PER_MESSAGE_CONSUME
            }
        }
        return counter
    }

    fun computeToken(roleMessages: List<RoleMessage>):Int{
        //计数器
        var counter = roleMessages.size * PER_MESSAGE_CONSUME
        roleMessages.forEach {
            counter += enc.countTokens(it.content)
        }
        return counter
    }

}
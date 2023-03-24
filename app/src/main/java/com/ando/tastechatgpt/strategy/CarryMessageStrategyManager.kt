package com.ando.tastechatgpt.strategy

import com.ando.tastechatgpt.domain.entity.ChatMessageEntity
import com.ando.tastechatgpt.domain.pojo.ChatContext

interface CarryMessageStrategyManager {
    val strategyList: List<String>
    fun <T> filterBy(strategy: String, block: CarryMessageStrategy.() -> T): T
    fun filterBy(strategy: String): (ChatMessageEntity, ChatContext)->Boolean
    fun addStrategy(name:String, carryMessageStrategyClass: Class<out CarryMessageStrategy>)
}
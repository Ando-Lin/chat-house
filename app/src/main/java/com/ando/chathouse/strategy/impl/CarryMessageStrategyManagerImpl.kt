package com.ando.chathouse.strategy.impl

import com.ando.chathouse.domain.entity.ChatMessageEntity
import com.ando.chathouse.domain.pojo.ChatContext
import com.ando.chathouse.exception.NoSuchStrategy
import com.ando.chathouse.strategy.CarryMessageStrategy
import com.ando.chathouse.strategy.CarryMessageStrategyManager
import com.ando.chathouse.strategy.StatefulCarryMessageStrategy

class CarryMessageStrategyManagerImpl : CarryMessageStrategyManager {
    private val map = mutableMapOf<String, Class<out CarryMessageStrategy>>()
    private val cache = mutableMapOf<String, CarryMessageStrategy>()


    override val strategyList: List<String>
        get() = map.map { it.key }

    override fun <T> filterBy(strategy: String, block: CarryMessageStrategy.() -> T): T {
        return block(getInstance(strategy))
    }

    override fun filterBy(strategy: String): (ChatMessageEntity, ChatContext) -> Boolean {
        val instance = getInstance(strategy)
        return instance::filter
    }

    override fun addStrategy(name: String, carryMessageStrategyClass: Class<out CarryMessageStrategy>) {
        map[name] = carryMessageStrategyClass
    }


    private fun getInstance(strategy: String): CarryMessageStrategy {
        val strategyClass = map[strategy] ?: throw NoSuchStrategy("策略不存在: strategy=$strategy")
        val strategyInstance: CarryMessageStrategy =
            if (StatefulCarryMessageStrategy::class.java.isAssignableFrom(strategyClass)) {
                strategyClass.newInstance()
            } else {
                cache.getOrPut(strategy) { strategyClass.newInstance() }
            }
        return strategyInstance
    }
}
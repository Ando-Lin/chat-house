package com.ando.chathouse.domain.pojo

/**
 * 用于将可变的显示名称与strategy进行映射
 */
data class StrategyMap(private val map: Map<String, String>){
    val nameList:List<String> = map.map { it.key }
    val strategyList:List<String> = map.map { it.value }
    fun getStrategy(name:String) = map[name]
    fun getName(strategy:String) = nameList.getOrNull(strategyList.indexOf(strategy))
}
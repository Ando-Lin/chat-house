package com.ando.tastechatgpt.strategy

interface MutableStatefulCarryMessageStrategy<T>:StatefulCarryMessageStrategy {
    val defaultState: T
    fun setState(value:T)
}
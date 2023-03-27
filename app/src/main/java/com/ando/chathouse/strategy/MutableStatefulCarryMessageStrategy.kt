package com.ando.chathouse.strategy

interface MutableStatefulCarryMessageStrategy<T>:StatefulCarryMessageStrategy {
    val defaultState: T
    fun setState(value:T)
}
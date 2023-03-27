package com.ando.chathouse.strategy

interface StatefulCarryMessageStrategy:CarryMessageStrategy {
    fun clearState()
}
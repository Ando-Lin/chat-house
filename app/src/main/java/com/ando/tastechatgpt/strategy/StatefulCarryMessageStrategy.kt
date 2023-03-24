package com.ando.tastechatgpt.strategy

interface StatefulCarryMessageStrategy:CarryMessageStrategy {
    fun clearState()
}
package com.ando.chathouse.domain.pojo

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

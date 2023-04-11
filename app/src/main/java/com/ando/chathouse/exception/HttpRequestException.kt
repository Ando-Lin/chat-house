package com.ando.chathouse.exception

class HttpRequestException : RuntimeException {
    constructor() : super(LABEL)
    constructor(code: Int) : super(LABEL + "code = $code")
    constructor(code: Int, cause: Throwable) : super(LABEL + "code = $code", cause)
    constructor(cause: Throwable) : super(LABEL, cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    companion object {
        const val LABEL = "HTTP请求出错"
    }
}
package com.ando.chathouse.constant

const val OPENAI_URL = "https://api.openai.com"
const val OPENAI_MIRROR_URL = "https://api.beebebusy.xyz"
const val CHAT_COMPLETIONS_URL_PATH = "/v1/chat/completions"
const val MY_UID = 1
const val WRITE_DB_TOKEN_THRESHOLD = 10   //个数
const val WRITE_DB_TIME_THRESHOLD = 500L //毫秒
//预留的token数
const val RESERVED_TOKEN = 100
//最大token数
const val MAX_TOKEN = 4097 * 0.95
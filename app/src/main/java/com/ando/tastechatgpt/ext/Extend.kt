package com.ando.tastechatgpt.ext

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.navigation.NavHostController
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


fun String.toAnnotatedString(): AnnotatedString {
    //TODO: 文本解析为注解文本
    val pattern = Pattern.compile("")
    val matcher = pattern.matcher(this)

    return AnnotatedString(text = this, spanStyle = SpanStyle())
}


fun LocalDateTime.formatByNow(context: Context): String {
    val now = LocalDateTime.now()
    val zonedDateTime = this.atZone(ZoneId.systemDefault())
    val epochMill = zonedDateTime.toEpochSecond() * 1000
    var flags =
        DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_NO_NOON_MIDNIGHT
    flags = when {
        //判断是否跨年，是则显示年
        //判断是否跨月，是则显示月份
        //判断是否是一天内，是则显示时间
        now.year - this.year > 0 -> {
            flags
        }
        now.dayOfYear - this.dayOfYear > 0 -> {
            flags or DateUtils.FORMAT_NO_YEAR
        }
        else -> {
            flags xor DateUtils.FORMAT_SHOW_DATE
        }
    }
    return DateUtils.formatDateTime(context, epochMill, flags)
}

fun NavHostController.navigateSingleTop(route: String){
    Log.i(TAG,"navigateSingleTop: route=$route")
    navigate(route = route) {
        launchSingleTop = true
        restoreState = true
    }
}

private const val TAG = "Extend"


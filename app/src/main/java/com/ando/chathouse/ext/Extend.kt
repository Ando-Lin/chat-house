package com.ando.chathouse.ext

import android.content.Context
import android.text.format.DateUtils
import android.widget.Toast
import androidx.collection.LruCache
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.navigation.NavHostController
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId


fun String.toAnnotatedString(): AnnotatedString {
    //TODO: 文本解析为注解文本
//    val pattern = Pattern.compile("")
//    val matcher = patthis.matcher(this)
    //替换字符换行
    var newText = this.replace("\\n", "\n")
    newText = newText.replace("\\r", "\r")
    return AnnotatedString(text = newText, spanStyle = SpanStyle())
}

fun LocalDateTime?.relativeToNowSecondDiff(): Pair<LocalDateTime, Long> {
    val now = LocalDateTime.now()
    val secondDiff = Duration.between(this ?: LocalDateTime.MIN, now).seconds
    return now to secondDiff
}

fun LocalDateTime.formatByNow(context: Context): String {
    val now = LocalDateTime.now()
    val zonedDateTime = this.atZone(ZoneId.systemDefault())
    val epochMill = zonedDateTime.toEpochSecond() * 1000
    var flags =
        DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_NO_NOON_MIDNIGHT
    flags = when {
        //判断是否跨年，是则显示年
        //判断是否跨日，是则显示日月份
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

fun NavHostController.navigateSingleTop(route: String) {
    navigate(route = route) {
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.navigatePopup(route: String) {
    val currentRoute = this.currentDestination?.route
    navigate(route = route) {
        launchSingleTop = true
        if (currentRoute!=null){
            popUpTo(route = currentRoute){
                inclusive = true
            }
        }
    }
}


suspend fun PressGestureScope.withMutableInteractionSource(
    offset: Offset,
    interactionSource: MutableInteractionSource,
    onRelease: (suspend () -> Unit)? = null,
    onCancel: (suspend () -> Unit)? = null
) {
    val press = PressInteraction.Press(offset)
    interactionSource.emit(press)
    val release = tryAwaitRelease()
    if (release) {
        onRelease?.invoke()
        interactionSource.emit(PressInteraction.Release(press))
    } else {
        onCancel?.invoke()
        interactionSource.emit(PressInteraction.Cancel(press))
    }
}


fun <K : Any, V : Any> LruCache<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    val value = get(key)
    return if (value == null) {
        val answer = defaultValue()
        put(key, answer)
        answer
    } else {
        value
    }
}


suspend fun Context.showToast(content: String) =
    Toast.makeText(this, content, Toast.LENGTH_SHORT).show()

private const val TAG = "Extend"


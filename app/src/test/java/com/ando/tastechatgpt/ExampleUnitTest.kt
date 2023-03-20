package com.ando.tastechatgpt

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofLocalizedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test1() {
        val now = LocalDateTime.now()
        val zonedDateTime = ZonedDateTime.of(now, ZoneId.systemDefault())
        println(zonedDateTime.toEpochSecond()*1000)
        println(System.currentTimeMillis())
    }
}
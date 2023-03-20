package com.ando.tastechatgpt.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ando.tastechatgpt.ext.formatByNow
import java.time.LocalDateTime

//@Preview(showBackground = true)
@Composable
fun PPP() {
    val context = LocalContext.current
    val now = LocalDateTime.now()
//    val s1 = remember {
//        val timeMillis = System.currentTimeMillis()
//        val formatFlag =
//            DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR or DateUtils.FORMAT_SHOW_TIME
//        mutableStateOf(DateUtils.formatDateTime(context, timeMillis, formatFlag))
//    }
//    val s2 = remember {
//            val timeMillis = System.currentTimeMillis()
//            val formatFlag =
//                DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_NO_NOON_MIDNIGHT
//            mutableStateOf(DateUtils.formatDateTime(context, timeMillis, formatFlag))
//        }
//    val s3 = remember {
//        val timeMillis = System.currentTimeMillis()
//        val formatFlag =
//            DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_NO_NOON_MIDNIGHT
//        mutableStateOf(DateUtils.formatDateTime(context, timeMillis, formatFlag))
//    }
//    Column {
//        Text(text = s1.value)
//        Text(text = s2.value)
//        Text(text = s3.value)
//    }
    Column {
        Text(text = now.minusHours(1).formatByNow(context))
        Text(text = now.minusDays(1).formatByNow(context))
        Text(text = now.minusYears(1).formatByNow(context))
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun TestPager() {
    Column {
        Text(
            text = "pager",
            modifier = Modifier
        )
        HorizontalPager(pageCount = 2, pageSize = PageSize.Fill) { currentPageId ->
            Text(
                text = "Page: $currentPageId",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
    }
}
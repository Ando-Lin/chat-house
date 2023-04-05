package com.ando.chathouse.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ando.chathouse.ui.component.dragfetch.DragData
import com.ando.chathouse.ui.component.dragfetch.DragFetchState
import com.ando.chathouse.ui.component.dragfetch.dragFetch
import com.ando.chathouse.ui.component.dragfetch.rememberDragFetchState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeLoading(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    isFetching: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onFetch: (() -> Unit)? = null,
    pullThreshold: Dp = 50.dp,
    liftThreshold:Dp = (-40).dp,
    refreshIndicator: @Composable (BoxScope.(DragFetchState)->Unit)?=null,
    fetchIndicator: @Composable (BoxScope.(DragFetchState)->Unit)?=null,
    content: @Composable () -> Unit
) {
    val pullRefreshState = rememberDragFetchState(
        fetching = isRefreshing,
        onFetch = onRefresh ?: {},
        dragData = DragData(
            dragThreshold = pullThreshold,
            dragOffset = pullThreshold,
            maxOverDragPercent = 0.5f
        )
    )
    val liftFetchState = rememberDragFetchState(
        fetching = isFetching,
        onFetch = onFetch ?: {},
        dragData = DragData(
            dragThreshold = liftThreshold,
            dragOffset = liftThreshold,
            maxOverDragPercent = 0.3f
        )
    )
    Box(
        modifier = modifier
            .wrapContentSize()
            .dragFetch(pullRefreshState, onRefresh != null)
            .dragFetch(liftFetchState, onFetch != null)
            .graphicsLayer {
                translationY = pullRefreshState.position + liftFetchState.position
            }
    ) {

        //下拉指示器
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -pullThreshold)
                .height(abs(pullThreshold.value).dp)
                .fillMaxWidth()
        ) {
            refreshIndicator?.invoke(this, pullRefreshState)
        }
        //上提指示器
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -liftThreshold)
                .height(abs(liftThreshold.value).dp)
                .fillMaxWidth()
        ) {
            fetchIndicator?.invoke(this, liftFetchState)
        }

        content()
    }
}

@Preview
@Composable
fun PreviewSwipeRefresh() {
    val modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .background(Color.Blue)
    val scope = rememberCoroutineScope()
    val scope2 = rememberCoroutineScope()
    var isRefreshing by remember {
        mutableStateOf(false)
    }
    var isFetching by remember {
        mutableStateOf(false)
    }
    val list = mutableListOf("1","2","3","4").toMutableStateList()
    SwipeLoading(
        onFetch = {
            isFetching = true
            scope.launch {
                delay(1500)
                isFetching = false
            }
        },
        onRefresh = {
            scope.launch {
                isRefreshing = true
                list.removeFirst()
                delay(100)
                isRefreshing = false
            }
        },
        isFetching = isFetching,
        isRefreshing = isRefreshing
    ) {
        LazyColumn() {
            items(list) { item ->
                Text(text = item, modifier = modifier)
            }
        }
    }

}

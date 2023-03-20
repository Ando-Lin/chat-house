package com.ando.tastechatgpt.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.domain.pojo.*
import com.ando.tastechatgpt.profile
import com.ando.tastechatgpt.ui.component.DialogForStringInput
import com.ando.tastechatgpt.ui.component.SimpleAlertDialog
import com.ando.tastechatgpt.ui.component.SimpleTopBar
import kotlinx.coroutines.launch


private val settingGroup: List<SettingItem<*>> by lazy {
    listOf(
        NightModeSetting,
        ApiKeySetting
    )
}

@Composable
fun SettingScreen(
    backAction: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    Surface(color = backgroundColor) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            SimpleTopBar(onBackClick = backAction, title = stringResource(id = R.string.setting))
            val context = LocalContext.current
            settingGroup.forEach { item ->
                item.dataStore = context.profile
                when (item.type) {
                    SettingItem.Type.String -> {
                        StringSetting(settingItem = item as BaseTypeSetting<String>)
                    }
                    else -> {}
                }
            }
        }
    }
}




@Composable
fun PlainSettingRow(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit = {
        Spacer(modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(20.dp) )
    }
) {
    Surface(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = modifier
                .padding(10.dp)
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StringSetting(
    settingItem: BaseTypeSetting<String>,
) {
    //配置里的原始值
    val originText = settingItem.value.collectAsState(initial = "")
    //弹窗可见性
    var dialogVisible by remember {
        mutableStateOf(false)
    }
    //执行写入配置的协程scope
    val scope = rememberCoroutineScope()

    //显示设置信息
    Surface {
        PlainSettingRow(
            text = stringResource(id = settingItem.nameResId) + ": ${originText.value}",
            onClick = { dialogVisible = true },
        )
    }

    //弹窗写入
    DialogForStringInput(
        dialogVisible = dialogVisible,
        onCancel = { dialogVisible = false },
        onConfirm = {
            scope.launch {
            runCatching {
                settingItem.onInputComplete(it)
            }.onFailure { TODO("写入失败，弹出通知") }
            dialogVisible = false
        } },
        initText = originText.value?:"",
        label = stringResource(id = settingItem.nameResId),
        placeholder = stringResource(id = R.string.input_on_here),
        modifier = Modifier.fillMaxWidth()
    )
}
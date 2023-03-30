@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ando.chathouse.ui.component.exclusive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ando.chathouse.R
import com.ando.chathouse.domain.pojo.StrategyMap
import com.ando.chathouse.ui.component.FilledTonalSquareIconButton
import com.ando.chathouse.ui.component.SegmentedButtonGroup
import kotlinx.coroutines.flow.Flow

data class ChatScreenSettingsUiState(
    private val modelListFlow: Flow<List<String>>,
    private val strategyMapFlow: Flow<StrategyMap>,
    private val currentModelFlow:Flow<String>,
    private val currentStrategyFlow:Flow<String>,
    private val editModeState: State<Boolean>,
    private val multiSelectModeState: State<Boolean>
){
    val editMode: Boolean by editModeState
    val multiSelectMode: Boolean by multiSelectModeState

    @Composable
    fun modelList() = modelListFlow.collectAsState(initial = listOf()).value

    @Composable
    fun strategyMap() = strategyMapFlow.collectAsState(initial = StrategyMap(emptyMap())).value

    @Composable
    fun currentModel() = currentModelFlow.collectAsState(initial = "").value

    @Composable
    fun currentStrategy() = currentStrategyFlow.collectAsState(initial = "").value
}

data class ChatScreenTopBarUiState(
    private val titleFlow: Flow<String>,
    private val settingsUiStateState: State<ChatScreenSettingsUiState>
){
    val settingsUiState by settingsUiStateState

    @Composable
    fun title() = titleFlow.collectAsState(initial = "").value
}

@Composable
fun ChatScreenExtendedTopBar(
    modifier: Modifier = Modifier,
    uiState: ChatScreenTopBarUiState,
    onClickMenu: () -> Unit,
    onClickActionIcon: () -> Unit,
    onSelectModel: (model: String) -> Unit,
    onSelectStrategy: (strategy: String) -> Unit,
    onMultiSelectModeChange: (Boolean) -> Unit,
    onClickClearConversation: () -> Unit
) {
    val title = uiState.title()
    val settingsUiState = uiState.settingsUiState
    Column(modifier) {
        ChatScreenTopBar(
            title =title,
            editMode = settingsUiState.editMode,
            onClickMenu = onClickMenu,
            onClickActionIcon = onClickActionIcon
        )
        ChatSettings(
            uiState = settingsUiState,
            onSelectModel = onSelectModel,
            onSelectStrategy = onSelectStrategy,
            onMultiSelectModeChange = onMultiSelectModeChange,
            onClickClearConversation = onClickClearConversation,
        )
    }
}


@Composable
private fun ChatScreenTopBar(
    modifier: Modifier = Modifier,
    title: String,
    editMode: Boolean,
    onClickMenu: () -> Unit,
    onClickActionIcon: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickMenu) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
            }
        },
        actions = {
            var contentDescriptionResId = R.string.edit_mode
            var icon = Icons.Default.Edit
            if (editMode) {
                icon = Icons.Default.Chat
                contentDescriptionResId = R.string.chat_mode
            }
            IconButton(
                onClick = onClickActionIcon,
                modifier = Modifier.scale(0.8f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(id = contentDescriptionResId),
                )
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSettings(
    modifier: Modifier = Modifier,
    uiState: ChatScreenSettingsUiState,
    onSelectModel: (model: String) -> Unit,
    onSelectStrategy: (strategy: String) -> Unit,
    onMultiSelectModeChange: (Boolean) -> Unit,
    onClickClearConversation: () -> Unit
) {
    Column(modifier) {
        AnimatedVisibility(visible = uiState.editMode) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SegmentedButtonGroup(
                    labelList = listOf(
                        stringResource(R.string.close_multi_select_mode),
                        stringResource(R.string.enable_multi_select_mode)
                    ),
                    selectedIndex = if (uiState.multiSelectMode) 1 else 0,
                    onSelect = { onMultiSelectModeChange(it == 1) },
                )
            }
        }
        AnimatedVisibility(visible = !uiState.multiSelectMode && uiState.editMode) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                //模型选择组
                val modelList = uiState.modelList()
                SettingMultiShortItem(
                    label = stringResource(id = R.string.model),
                    items = uiState.modelList(),
                    selectedItem = uiState.currentModel(),
                    onSelect = { onSelectModel(modelList[it]) },
                )

                Divider(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )

                //策略选择组
                val strategyMap = uiState.strategyMap()
                val selectedItem = strategyMap.getName(uiState.currentStrategy())
                SettingMultiShortItem(
                    label = stringResource(id = R.string.strategies),
                    items = strategyMap.nameList,
                    selectedItem = selectedItem?:"",
                    onSelect = {
                        val name = strategyMap.nameList[it]
                        onSelectStrategy(strategyMap.getStrategy(name)!!)
                    }
                )

                Divider(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )


                //单一功能按钮组
                Row(Modifier.padding(vertical = 10.dp)) {

                    FilledTonalSquareIconButton(
                        onClick = onClickClearConversation,
                        label = { Text(text = stringResource(id = R.string.clear_the_conversation)) },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(
                                id = R.string.clear_the_conversation
                            )
                        )
                    }
                }

            }
        }
    }

}


@Composable
fun SettingMultiShortItem(
    modifier: Modifier = Modifier,
    label: String,
    items: List<String>,
    selectedItem: String,
    headContent: @Composable (RowScope.()->Unit)?=null,
    tailContent: @Composable (RowScope.()->Unit)? = null,
    onSelect: (index: Int) -> Unit
) {
    MultiShortItem(modifier = modifier, label = label) {
        headContent?.invoke(this)
        items.forEachIndexed { index, item ->
            val selected = item == selectedItem
            FilterChip(
                selected = selected,
                onClick = { onSelect(index) },
                label = { Text(text = item) },
                modifier = Modifier.padding(horizontal = 4.dp),
                leadingIcon = {
                    AnimatedVisibility(visible = selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
        tailContent?.invoke(this)
    }
}

@Composable
fun MultiShortItem(
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 5.dp, top = 12.dp)
        )
        FlowRow(modifier = Modifier.wrapContentSize()) {
            content()
        }
    }
}
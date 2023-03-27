package com.ando.chathouse.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtonGroup(
    modifier: Modifier = Modifier,
    labelModifier: Modifier = Modifier,
    labelList: List<String>,
    iconList: List<ImageVector>? = null,
    selectedIndex: Int,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    border: SelectableChipBorder = segmentedButtonBorder(),
    onSelect: (index: Int) -> Unit
) {
    Row(modifier = modifier) {
        labelList.forEachIndexed { index, item ->
            val left = if (index == 0) 100 else 0
            val right = if (index == labelList.size - 1) 100 else 0
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                shape = AbsoluteRoundedCornerShape(
                    topLeftPercent = left,
                    bottomLeftPercent = left,
                    topRightPercent = right,
                    bottomRightPercent = right
                ),
                label = {
                    Text(
                        text = item,
                        modifier = labelModifier
                    )
                },
                leadingIcon = {
                    val icon = iconList?.getOrNull(index)
                    if (icon!=null){
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                colors = colors,
                border = border,
                modifier = Modifier
                    .offset(x = (-1 * index).dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun segmentedButtonBorder() = FilterChipDefaults.filterChipBorder(
    selectedBorderColor = colorScheme.outline,
    selectedBorderWidth = 1.dp,
    borderWidth = 1.dp
)
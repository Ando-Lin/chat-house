package com.ando.tastechatgpt.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import com.ando.tastechatgpt.R

@Composable
fun TextAndSwitch(
    modifier: Modifier = Modifier,
    title: String,
    tip: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean)->Unit,
) {
    Row(modifier = modifier) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            tip?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.9f)
        )
    }
}
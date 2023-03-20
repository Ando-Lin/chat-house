package com.ando.tastechatgpt.ui.component

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.SettingScreenDestination
import com.ando.tastechatgpt.ui.theme.TasteChatGPTTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    navigateAction: (route: String) -> Unit,
    drawerContent: @Composable ColumnScope.() -> Unit,
    enable: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!enable){
        content()
        return
    }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val drawerShape = RoundedCornerShape(10.dp)
    if (isLandscape) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(drawerShape = drawerShape, modifier = modifier.fillMaxWidth(0.3f)) {
                    drawerContent()
                    TDrawerContent(navigateAction = navigateAction)
                }
            },
            content = content
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerShape = drawerShape, modifier = modifier.fillMaxWidth(0.65f)) {
                    drawerContent()
                    TDrawerContent(navigateAction = navigateAction)
                }
            },
            content = content
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.TDrawerContent(
    modifier: Modifier = Modifier,
    navigateAction: (route: String) -> Unit,
) {

    Spacer(modifier = Modifier.weight(1f))
    Divider(Modifier.padding(horizontal = 20.dp))
    NavigationDrawerItem(
        label = { Text(text = "设置", style = MaterialTheme.typography.labelLarge) },
        selected = false,
        onClick = { navigateAction(SettingScreenDestination.route) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_outline_settings_24),
                contentDescription = null
            )
        },
        modifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .padding(vertical = 10.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TDrawerPre() {
    TasteChatGPTTheme {
        TDrawer(
            navigateAction = {},
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            drawerContent = {}
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}
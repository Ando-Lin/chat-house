package com.ando.chathouse.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ando.chathouse.R
import com.ando.chathouse.SettingScreenDestination
import com.ando.chathouse.ui.theme.ChatHouseTheme

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
        LaunchedEffect(Unit){
            drawerState.close()
        }
    }
    val drawerShape = RoundedCornerShape(7.dp)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = enable,
        drawerContent = {
            ModalDrawerSheet(drawerShape = drawerShape, modifier = modifier.requiredWidth(260.dp)) {
                drawerContent()
                TDrawerContent(navigateAction = navigateAction)
            }
        },
        content = content
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.TDrawerContent(
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
        modifier = modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .padding(vertical = 10.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TDrawerPre() {
    ChatHouseTheme {
        TDrawer(
            navigateAction = {},
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            drawerContent = {}
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}
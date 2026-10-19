package com.dusrel.shrinkit.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.history.HistoryScreen
import com.dusrel.shrinkit.presentation.home.HomeScreen
import com.dusrel.shrinkit.presentation.settings.SettingsScreen

private data class BottomTab(
    val icon: ImageVector,
    val labelRes: Int
)

/**
 * Bottom-navigation scaffold hosting the Home, History and Settings tabs.
 * The Tool and CropEditor are full-screen destinations outside the tabs.
 */
@Composable
fun MainScreen(
    toolViewModel: ShrinkItViewModel,
    onOpenTool: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        BottomTab(Icons.Filled.Home, R.string.tab_home),
        BottomTab(Icons.Filled.History, R.string.tab_history),
        BottomTab(Icons.Filled.Settings, R.string.tab_settings)
    )
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                toolViewModel = toolViewModel,
                onOpenTool = onOpenTool,
                onOpenHistory = { selectedTab = 1 },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> HistoryScreen(
                modifier = Modifier.padding(innerPadding)
            )
            else -> SettingsScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

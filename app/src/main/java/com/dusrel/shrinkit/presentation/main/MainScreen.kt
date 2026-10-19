package com.dusrel.shrinkit.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.presentation.history.HistoryScreen
import com.dusrel.shrinkit.presentation.home.HomeScreen
import com.dusrel.shrinkit.presentation.settings.SettingsScreen

private data class BottomTab(
    val iconName: String,
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
    onOpenTools: () -> Unit = {},
    onOpenSettingsDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        BottomTab("home", R.string.tab_home),
        BottomTab("history", R.string.tab_history),
        BottomTab("settings", R.string.tab_settings)
    )
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Web parity (cross-verified): bottom bar uses the page background
            // with a hairline top border — not M3's default surfaceContainer
            // tonal tint. containerColor override is API-supported.
            Column {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                SymbolIcon(
                                    name = tab.iconName,
                                    contentDescription = null,
                                    filled = selectedTab == index
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                toolViewModel = toolViewModel,
                onOpenTool = onOpenTool,
                onOpenHistory = { selectedTab = 1 },
                onOpenTools = onOpenTools,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> HistoryScreen(
                modifier = Modifier.padding(innerPadding)
            )
            else -> SettingsScreen(
                onNavigate = onOpenSettingsDetail,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

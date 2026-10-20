package com.dusrel.shrinkit.presentation.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.components.SymbolIcon

/**
 * Single-purpose tool entry, mirroring the web app's /tools pages.
 */
data class ToolEntry(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val iconName: String
)

val ALL_TOOLS = listOf(
    ToolEntry("kb50", R.string.tool_kb50_title, R.string.tool_kb50_desc, "speed"),
    ToolEntry("kb20", R.string.tool_kb20_title, R.string.tool_kb20_desc, "bolt"),
    ToolEntry("passport", R.string.tool_passport_title, R.string.tool_passport_desc, "photo_size_select_large"),
    ToolEntry("social", R.string.tool_social_title, R.string.tool_social_desc, "share"),
    ToolEntry("compare", R.string.tool_compare_title, R.string.tool_compare_desc, "compare")
)

/**
 * Hub listing the 5 single-purpose tools (web parity: the /tools pages).
 */
@Composable
fun ToolsHubScreen(
    onBack: () -> Unit,
    onOpenTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.tools_title),
                subtitle = stringResource(R.string.tools_subtitle),
                onBackClick = onBack
            )
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp, vertical = 8.dp
            )
        ) {
            items(ALL_TOOLS, key = { it.id }) { tool ->
                ToolHubRow(
                    tool = tool,
                    onClick = { onOpenTool(tool.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun ToolHubRow(
    tool: ToolEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(end = 0.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(12.dp)
        ) {
            SymbolIcon(
                name = tool.iconName,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(tool.titleRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(tool.descRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SymbolIcon(
            name = "chevron_right",
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

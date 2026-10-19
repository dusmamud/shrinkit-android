package com.dusrel.shrinkit.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val iconName: String,
    val titleRes: Int,
    val bodyRes: Int,
    val filled: Boolean = true
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPage(
            "shield",
            R.string.onboarding_privacy_title,
            R.string.onboarding_privacy_body
        ),
        OnboardingPage(
            "photo_size_select_large",
            R.string.onboarding_target_title,
            R.string.onboarding_target_body
        ),
        OnboardingPage(
            "bolt",
            R.string.onboarding_batch_title,
            R.string.onboarding_batch_body
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.size - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (!isLast) {
                TextButton(onClick = onFinished) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        SymbolIcon(
                            name = page.iconName,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            filled = page.filled,
                            fontSize = 64.sp
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    text = stringResource(page.titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(page.bodyRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            repeat(pages.size) { index ->
                val selected = index == pagerState.currentPage
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape),
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                ) {}
            }
        }

        Button(
            onClick = {
                if (isLast) {
                    onFinished()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = LocalButtonContainer.current),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (isLast) R.string.onboarding_get_started
                    else R.string.onboarding_next
                )
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

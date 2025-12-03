package com.example.hearablemusicplayer.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DotPager(
    pageContent: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    initialPage: Int = 0
) {
    val pageCount = pageContent.size
    // 基础滑动页
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )
    Column (modifier = modifier) {
        HorizontalPager(state = pagerState) { page ->
            Box(
                Modifier.fillMaxWidth()
                    .height(410.dp),
                contentAlignment = Alignment.Center
            ) {
                pageContent[page]()
            }
        }

        // 极简圆点指示器（仅多页时显示）
        if (pageCount > 1) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(pageCount) { index ->
                        val size = 6.dp
                        val color = if (pagerState.currentPage == index)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

                        Box(
                            modifier = Modifier
                                .size(size)
                                .background(color, CircleShape)
                        )
                    }
                }
            }
        }
    }
}


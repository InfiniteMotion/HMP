package com.hmp.storybook.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

/**
 * 手机框架预览组件，在桌面浏览器中模拟手机屏幕
 */
@Composable
fun PhoneFramePreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 根据容器宽度自适应手机尺寸
        val phoneWidth = max(minOf(maxWidth * 0.45f, 380.dp), 280.dp)
        val phoneHeight = phoneWidth * 2.1f // ~19.5:9 比例

        Box(
            modifier = Modifier
                .width(phoneWidth)
                .height(phoneHeight)
                .clip(RoundedCornerShape(32.dp))
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(32.dp),
                )
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            // 刘海
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .width(phoneWidth * 0.3f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            // 内容区
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
            ) {
                content()
            }
        }
    }
}

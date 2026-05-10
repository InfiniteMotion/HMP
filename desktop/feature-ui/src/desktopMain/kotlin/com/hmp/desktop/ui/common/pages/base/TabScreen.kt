package com.hmp.desktop.ui.common.pages.base
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.hmp.desktop.ui.common.layout.widthSizeClass
import com.hmp.desktop.ui.common.layout.WindowWidthSizeClass

import com.hmp.desktop.ui.common.components.base.SearchButton


val LocalTabHeaderContent = staticCompositionLocalOf<(@Composable () -> Unit)?> { null }

@Composable
fun TabScreen(
    title: String? = null,
    hasSearchBotton: Boolean = false,
    navController: NavController? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val windowWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
    val sizeClass = widthSizeClass(windowWidthDp)

    // 扩展布局使用更大的水平内边距
    val horizontalPadding = when (sizeClass) {
        WindowWidthSizeClass.Expanded -> 48.dp
        WindowWidthSizeClass.Medium -> 32.dp
        WindowWidthSizeClass.Compact -> 16.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (title != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        if (trailing != null) {
                            trailing()
                        }

                        if (hasSearchBotton && navController != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            SearchButton(navController)
                        }
                    }
                }
            }
            content()
        }
    }
}
